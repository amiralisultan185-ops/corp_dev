# Product

We build an appointment booking system.
People use it to request appointments and track their progress.

# Core item

The tracked item is an appointment.

Each appointment has an AppointmentId that cannot be null, empty,
or contain only whitespace.

AppointmentStatus is a sealed interface with three variants:

- Requested — an appointment has been requested.
- Confirmed — the appointment time has been agreed upon and confirmed.
- Completed — the appointment has taken place.

AppointmentPolicy.move(from, to) returns the target status for an
allowed transition and throws IllegalStateException otherwise.

# Status table

| From | To | Allowed | Business reason for prohibition |
|------|----|---------|---------------------------------|
| Requested | Confirmed | Yes | — |
| Confirmed | Completed | Yes | — |
| Requested | Completed | No | The appointment time must be agreed upon and confirmed before the appointment can be completed. |
| Completed | Requested | No | A completed appointment must remain a historical record. Another visit requires a new appointment. |

# Forbidden — why

Requested → Completed skips confirmation. The appointment cannot
be completed before its time has been agreed upon and confirmed.

Completed → Requested would reuse a finished appointment.
A new visit requires a new appointment with its own identity.

These four transitions are covered by the @CsvSource table
in AppointmentPolicyTest. AppointmentIdTest checks that valid
identifiers are accepted and null, empty, or whitespace-only
identifiers are rejected.

The project uses Java 21 and Maven, without Spring, REST, or a database.

Run all tests from this lab directory:

    mvn -q test
