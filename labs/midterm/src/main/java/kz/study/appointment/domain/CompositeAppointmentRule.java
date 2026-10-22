package kz.study.appointment.domain;

import java.util.List;

public final class CompositeAppointmentRule implements AppointmentRule {
    private final List<AppointmentRule> rules;

    public CompositeAppointmentRule(AppointmentRule... rules) {
        this.rules = List.of(rules);
    }

    @Override
    public void check(AppointmentId appointmentId, AppointmentStatus from, AppointmentStatus to) {
        rules.forEach(rule -> rule.check(appointmentId, from, to));
    }
}