package kz.study.appointment.domain;

import java.util.Objects;

public record Appointment(AppointmentId id, String businessKey,
                          AppointmentStatus status, String title) {
    public Appointment {
        Objects.requireNonNull(id, "Appointment id is required");
        Objects.requireNonNull(status, "Appointment status is required");
        if (businessKey == null || businessKey.isBlank()) {
            throw new IllegalArgumentException("Appointment business key must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Appointment title must not be blank");
        }
    }
}
