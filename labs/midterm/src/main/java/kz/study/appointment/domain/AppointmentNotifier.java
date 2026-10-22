package kz.study.appointment.domain;

public interface AppointmentNotifier {
    void statusChanged(AppointmentId id, AppointmentStatus status);
}
