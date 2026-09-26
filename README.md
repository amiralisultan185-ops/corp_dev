# Corporate Development Labs

One Git repository for the whole semester. Each lab lives in its own
folder under `labs/`, so new assignments can be added without mixing
their Maven projects.

## Labs

| Lab | Topic | Check command |
|-----|-------|---------------|
| [lab-01](labs/lab-01) | Appointment statuses in Java 21 | `cd labs/lab-01 && mvn -q test` |
| [lab-02](labs/lab-02) | Join Spring Boot to the appointment product | `cd labs/lab-02 && mvn -q verify` |

## Structure

- `labs/lab-01` - first lab: one tracked appointment item, statuses,
  transition policy, and tests.
- `labs/lab-02` - second lab: Spring Boot starts the same appointment
  product, while domain rules stay plain Java.
- Future labs should be added as `labs/lab-03`, `labs/lab-04`, and so on.
