package kz.study.appointment;

public sealed interface AppointmentStatus permits AppointmentStatus.Requested, AppointmentStatus.Confirmed, AppointmentStatus.Completed {
    record Requested() implements AppointmentStatus{}
    record Confirmed() implements AppointmentStatus{}
    record Completed() implements AppointmentStatus{}
}
