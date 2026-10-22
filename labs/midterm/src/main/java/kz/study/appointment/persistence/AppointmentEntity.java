package kz.study.appointment.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kz.study.appointment.domain.Appointment;
import kz.study.appointment.domain.AppointmentId;
import kz.study.appointment.domain.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment")
public class AppointmentEntity {
    @Id
    private UUID id;

    @Column(name = "business_key", nullable = false, unique = true, columnDefinition = "text")
    private String businessKey;

    @Column(nullable = false, columnDefinition = "text")
    private String status;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    // PostgreSQL supplies the timestamp through DEFAULT now().
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected AppointmentEntity() {
        // Required by JPA; field access is selected by @Id on the field.
    }

    static AppointmentEntity fromDomain(Appointment appointment) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.id = appointment.id().value();
        entity.businessKey = appointment.businessKey();
        entity.status = statusToText(appointment.status());
        entity.title = appointment.title();
        return entity;
    }

    Appointment toDomain() {
        return new Appointment(new AppointmentId(id), businessKey, statusFromText(status), title);
    }

    static String statusToText(AppointmentStatus status) {
        return switch (status) {
            case AppointmentStatus.Requested ignored -> "REQUESTED";
            case AppointmentStatus.Confirmed ignored -> "CONFIRMED";
            case AppointmentStatus.Completed ignored -> "COMPLETED";
        };
    }

    static AppointmentStatus statusFromText(String status) {
        return switch (status) {
            case "REQUESTED" -> new AppointmentStatus.Requested();
            case "CONFIRMED" -> new AppointmentStatus.Confirmed();
            case "COMPLETED" -> new AppointmentStatus.Completed();
            default -> throw new IllegalArgumentException("Unknown appointment status: " + status);
        };
    }
}
