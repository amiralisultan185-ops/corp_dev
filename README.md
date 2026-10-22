# Corporate Development Labs

One Git repository for the whole semester. Each lab lives in its own
folder under `labs/`, so new assignments can be added without mixing
their Maven projects.

## Labs

| Lab | Topic | Check command |
|-----|-------|---------------|
| [lab-01](labs/lab-01) | Appointment statuses in Java 21 | `cd labs/lab-01 && mvn -q test` |
| [lab-02](labs/lab-02) | Join Spring Boot to the appointment product | `cd labs/lab-02 && mvn -q verify` |
| [lab-03](labs/lab-03) | Tests, PostgreSQL, JDBC, and transactions | Start PostgreSQL, then `cd labs/lab-03 && mvn -q verify` |
| [midterm](labs/midterm) | Flyway migrations and Hibernate ORM | Start PostgreSQL, then `cd labs/midterm && mvn verify` |

## Structure

- `labs/lab-01` - first lab: one tracked appointment item, statuses,
  transition policy, and tests.
- `labs/lab-02` - second lab: Spring Boot starts the same appointment
  product, while domain rules stay plain Java.
- `labs/lab-03` - third lab: mocked notification port, hand-written SQL,
  JDBC persistence, and transaction tests against PostgreSQL.
  See its README for database setup and `DEFENSE-RU.md` for defense notes.
- Future labs should be added as `labs/lab-04`, `labs/lab-05`, and so on.
