# Lab 2 - Join Spring Boot to the Appointment Product

This lab continues the same product from Lab 1: an appointment booking
system. People use it to request appointments and track their progress.

## Goal

Join Spring Boot to the existing appointment domain without changing the
product theme and without adding REST, a database, or Docker.

Spring Boot should live at the edge of the application. The domain package
must stay plain Java and must not import `org.springframework`.

## Package Diagram

```text
dto          client        handler         config
(JSON later) (HTTP later) (HTTP week 9)   Application
     \            \            /              |
      \            \          /          injects Rule
       \            \        /
                  domain
      AppointmentId  AppointmentStatus  AppointmentPolicy
      AppointmentRule + two implementations
```

Arrows point inward. Domain never imports `org.springframework`.

## Planned Packages

- `dto` - request/response shapes, vendor JSON shapes later.
- `domain` - `AppointmentId`, `AppointmentStatus`, `AppointmentPolicy`,
  `AppointmentRule`, and rule implementations.
- `client` - outbound clients later; may stay empty in this lab.
- `handler` - Spring service layer for this lab.
- `config` - Spring Boot `Application` and bean configuration.

## Checklist

- [ ] Keep the same appointment product from Lab 1.
- [ ] Add Spring Boot parent/dependencies to `pom.xml`.
- [ ] Move domain types into the `domain` package.
- [ ] Add `Application` in `config`.
- [ ] Add `AppointmentService` with `@Service` in `handler`.
- [ ] Extract `AppointmentRule`.
- [ ] Add two rule implementations.
- [ ] Wire the rule chain with a `@Bean` in `config`.
- [ ] Confirm `domain` has no `org.springframework` import.
- [ ] Run `mvn -q verify`.
- [ ] Run `mvn spring-boot:run`.

## Commands

```powershell
mvn -q verify
mvn spring-boot:run
```
