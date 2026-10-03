# Lab 3 — Appointment: tests, PostgreSQL, JDBC

This module continues the appointment booking product from Labs 1 and 2 in the
same semester repository. Lab 2 is preserved in its own folder.

## Requirements

- Java 21 or newer (Maven compiles with `--release 21`).
- Maven, either installed or bundled with IntelliJ IDEA.
- PostgreSQL. No in-memory database is used by the integration tests.

The database defaults are `jdbc:postgresql://localhost:55432/css`, user `css`,
password `css`. Port 55432 avoids the existing PostgreSQL installation on 5432.
These are classroom credentials for a local database.

## Start the database

Run commands from `labs/lab-03`.

On this Windows computer, PostgreSQL 18 is already installed:

```powershell
.\tools\start-local-postgres.ps1
```

The script creates a separate cluster inside the ignored `.local-postgres`
directory. It starts PostgreSQL on 127.0.0.1:55432 and creates the css database.
It can be run again to start the same cluster after a restart.
Pass `-PgBin 'C:\Program Files\PostgreSQL\18\bin'` for a different binary path.

Alternatively, with Docker Desktop running:

```powershell
docker compose up -d --wait
```

Choose one database option: both use port 55432. Compose runs only PostgreSQL;
there is no Docker image of the Java application.

For another local PostgreSQL database, override the connection:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/css'
$env:DB_USER = 'css'
$env:DB_PASSWORD = 'css'
```

## Verify

```powershell
mvn -q verify
```

In IntelliJ, add this module's `pom.xml` as a Maven project, then run
**Lifecycle → verify** for `lab-03`.

To run only the Week 4 tests without connecting to PostgreSQL:

```powershell
mvn -q "-Dtest=AppointmentPolicyTest,AppointmentIdTest,AppointmentServiceTest" test
```

Integration tests create the dedicated schema `appointment_lab3_test`,
drop only its appointment table before each test, and execute the authored
`src/main/resources/db/schema.sql`. They do not change the public schema.
Use a local coursework database; the tests require permission to create a schema.
There is no `@Transactional` on a test method or test class, and no database tests
are silently skipped when PostgreSQL is unavailable.

Surefire XML and text reports are in `target/surefire-reports`.

## Product and status table

Each appointment has a technical UUID (`AppointmentId.newId()`), a human
business key such as `APT-1042`, a status, and a title.

| From | To | Allowed | Business reason for prohibition |
| --- | --- | --- | --- |
| Requested | Confirmed | Yes | — |
| Confirmed | Completed | Yes | — |
| Requested | Completed | No | The appointment must be confirmed before it takes place. |
| Completed | Requested | No | A completed appointment is history; another visit needs a new record. |

These exact four rows remain in `AppointmentPolicyTest.@CsvSource`.
The SQL status values are `REQUESTED`, `CONFIRMED`, and `COMPLETED`.
The mapper explicitly converts between those strings and the sealed records.
There are no additional fields in the three status records.

In Lab 3, `AppointmentId` wraps a UUID instead of the old string `apt-001`.
The readable number now belongs in `business_key`, not in the primary key.
Parsing rejects null, empty, blank, and malformed identifiers; creating a record
with a null UUID also fails. Java rejects null or blank business keys and titles.

## Architecture

```text
config/Application
        |
handler/AppointmentService
        |-- domain/AppointmentRule -> CompositeAppointmentRule -> two rules
        |-- domain/AppointmentNotifier -> config/LoggingAppointmentNotifier
        |-- domain/AppointmentRepository -> persistence/AppointmentJdbc
                                                    |
                                              JdbcTemplate
                                                    |
                                               PostgreSQL
```

- `domain`: records, rules, ports, and the unchecked `DuplicateAppointment`.
  It imports neither JDBC nor Spring.
- `persistence`: parameterized SQL with `?`, INSERT, count, and lookup.
  SQL resources, connections, and row mapping stay here.
- `handler`: transactional use cases and translation of Spring's
  `DuplicateKeyException` (a `DataIntegrityViolationException` subtype) into
  `DuplicateAppointment`. The original cause is retained.
- `config`: Spring wiring and a local logging implementation of the notifier.
- `move` retains the Lab 2 operation: validate an in-memory transition, notify,
  and return its target. It does not update a saved row.
- `register` and `insertTwice` are the new persistence operations.

`AppointmentServiceTest` uses a Mockito mock of the outbound notifier, with
real domain rules. It verifies notification after an allowed move and no
notification after a forbidden move. No HTTP request or email is sent.

## Schema and manual rollback demonstration

`src/main/resources/db/schema.sql` defines exactly one table:

- `id uuid PRIMARY KEY`: surrogate identity generated in Java.
- `business_key text NOT NULL UNIQUE`: the process number.
- `status text NOT NULL` plus a CHECK listing all three statuses.
- `title text NOT NULL`.
- `created_at timestamptz NOT NULL DEFAULT now()`.

The table is written by hand; no ORM or migration generator creates it.
To create the public table in a fresh database and prove rollback with psql:

```powershell
$env:PGPASSWORD = 'css'
& 'C:\Program Files\PostgreSQL\18\bin\psql.exe' -X -h 127.0.0.1 -p 55432 -U css -d css -v ON_ERROR_STOP=1 -f src/main/resources/db/schema.sql
& 'C:\Program Files\PostgreSQL\18\bin\psql.exe' -X -h 127.0.0.1 -p 55432 -U css -d css -v ON_ERROR_STOP=0 -f sql/rollback-demo.sql
```

Apply `schema.sql` once to an empty public schema. If the table already exists,
reuse it; do not delete existing data merely to rerun this command.
The demo requires its business key to be unused (the opening count must be 0).
Its second INSERT intentionally fails with a duplicate-key error. psql continues
to the explicit ROLLBACK, and `after_rollback` must be 0.

The same files can be passed to the container:

```powershell
Get-Content -Raw src/main/resources/db/schema.sql | docker compose exec -T postgres psql -X -U css -d css -v ON_ERROR_STOP=1
Get-Content -Raw sql/rollback-demo.sql | docker compose exec -T postgres psql -X -U css -d css -v ON_ERROR_STOP=0
```

## Why the two transaction tests have opposite counts

| Test | Transactions | Failure | Result |
| --- | --- | --- | --- |
| `secondStatementRollsBack` | One service call, one transaction, two INSERTs | Second INSERT duplicates the key | 0 rows |
| `secondRequestKeepsTheFirst` | Two service calls, one transaction per call | Second call duplicates the key | 1 row; the original ID and title survive |

The tests call the Spring-managed service proxy. An unchecked exception leaves
the transactional method, so Spring rolls back that call. There is no catch that
returns normally, no self-invocation between transactional methods, and no delete
used to hide duplicates. Counts are read after the service call has finished,
outside its transaction.

Additional PostgreSQL checks cover every status round trip, the CHECK constraint
(SQLState 23514), NOT NULL (23502), duplicate keys (23505), quoted input using
placeholders, and rejection of unknown database status text.

## Application and database lifecycle

After applying the public schema, `mvn spring-boot:run` starts the non-web Spring
application. There is no REST endpoint in this lab. The integration tests are the
executable demonstrations of registration and rollback.

Stop the native database when finished:

```powershell
& 'C:\Program Files\PostgreSQL\18\bin\pg_ctl.exe' -D .local-postgres -m fast -w stop
```

Or stop the Compose database with `docker compose stop`. The data is retained.

## Submission checklist

- Same product, same repository; this module is `labs/lab-03`.
- Four parameterized status rows; null/blank ID tests; mocked outbound port.
- Authored schema with UUID primary key, UNIQUE business key, status CHECK.
- JDBC INSERT and count use placeholders.
- Both required transactional outcomes tested against PostgreSQL.
- Domain has no Spring or JDBC imports.
- No JPA, Hibernate, database migration framework, REST, security framework,
  in-memory replacement database, or application Docker image.
- README and Russian defense notes: `DEFENSE-RU.md`.
- Run `mvn -q verify` with PostgreSQL before submission.
- Use your real Jira story ID in a PR title if submitting through a PR;
  this module does not invent a story ID.
- Build outputs, local database files, and IDE settings are ignored by Git.

Detailed Russian explanation of every file and important source lines:
`CODE-WALKTHROUGH-RU.md`.
