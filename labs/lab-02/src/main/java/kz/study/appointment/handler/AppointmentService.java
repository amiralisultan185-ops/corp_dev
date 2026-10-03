package kz.study.appointment.handler;

import kz.study.appointment.domain.AppointmentId;
import kz.study.appointment.domain.AppointmentRule;
import kz.study.appointment.domain.AppointmentStatus;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {
    private final AppointmentRule rules;

    public AppointmentService(AppointmentRule rules) {
        this.rules = rules;
    }

    public AppointmentStatus move(AppointmentId appointmentId, AppointmentStatus from, AppointmentStatus to) {
        rules.check(appointmentId, from, to);
        return to;
    }
}