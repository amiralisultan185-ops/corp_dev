package kz.study.appointment.domain;

public interface AppointmentRule {
    void check(AppointmentId appointmentId, AppointmentStatus from, AppointmentStatus to);
}
