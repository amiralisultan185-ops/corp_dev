package kz.study.appointment.domain;

public final class CompletedAppointmentCannotMoveRule implements AppointmentRule {
    @Override
    public void check(AppointmentId appointmentId, AppointmentStatus from, AppointmentStatus to) {
        if (from instanceof AppointmentStatus.Completed) {
            throw new IllegalStateException(
                    "Completed appointment " + appointmentId.value() + " cannot be moved"
            );
        }

    }

}
