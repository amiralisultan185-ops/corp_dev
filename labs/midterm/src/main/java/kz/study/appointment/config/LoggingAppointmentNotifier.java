package kz.study.appointment.config;

import kz.study.appointment.domain.AppointmentId;
import kz.study.appointment.domain.AppointmentNotifier;
import kz.study.appointment.domain.AppointmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingAppointmentNotifier implements AppointmentNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingAppointmentNotifier.class);

    @Override
    public void statusChanged(AppointmentId id, AppointmentStatus status) {
        LOG.info("Appointment {} status changed to {}", id.value(), status);
    }
}
