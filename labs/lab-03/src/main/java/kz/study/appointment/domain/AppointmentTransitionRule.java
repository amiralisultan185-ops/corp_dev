package kz.study.appointment.domain;

public final class AppointmentTransitionRule implements AppointmentRule {
    private final AppointmentPolicy policy;

    public AppointmentTransitionRule(AppointmentPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void check(AppointmentId appointmentId, AppointmentStatus from, AppointmentStatus to) {
        policy.move(from, to);
    }
}
