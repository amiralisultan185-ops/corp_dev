package kz.study.appointment.domain;

public class DuplicateAppointment extends RuntimeException {
    public DuplicateAppointment(String businessKey, Throwable cause) {
        super("Duplicate appointment: " + businessKey, cause);
    }
}
