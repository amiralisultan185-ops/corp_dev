package kz.study.appointment.domain;

import java.util.UUID;

public record AppointmentId(UUID value) {
    public AppointmentId {
        if (value == null) {
            throw new IllegalArgumentException("Appointment id must not be null");
        }
    }

    public static AppointmentId newId() {
        return new AppointmentId(UUID.randomUUID());
    }

    public static AppointmentId parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Appointment id must not be blank");
        }
        UUID uuid = UUID.fromString(value);
        if (!uuid.toString().equalsIgnoreCase(value)) {
            throw new IllegalArgumentException("Appointment id must be a full UUID");
        }
        return new AppointmentId(uuid);
    }
}
