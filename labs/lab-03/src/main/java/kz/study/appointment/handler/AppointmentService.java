package kz.study.appointment.handler;

import kz.study.appointment.domain.AppointmentId;
import kz.study.appointment.domain.AppointmentRule;
import kz.study.appointment.domain.AppointmentStatus;
import kz.study.appointment.domain.Appointment;
import kz.study.appointment.domain.AppointmentNotifier;
import kz.study.appointment.domain.AppointmentRepository;
import kz.study.appointment.domain.DuplicateAppointment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final AppointmentRule rules;
    private final AppointmentNotifier notifier;
    private final AppointmentRepository appointments;

    public AppointmentService(AppointmentRule rules, AppointmentNotifier notifier,
                              AppointmentRepository appointments) {
        this.rules = rules;
        this.notifier = notifier;
        this.appointments = appointments;
    }

    public AppointmentStatus move(AppointmentId appointmentId, AppointmentStatus from, AppointmentStatus to) {
        rules.check(appointmentId, from, to);
        notifier.statusChanged(appointmentId, to);
        return to;
    }

    @Transactional
    public Appointment register(AppointmentId id, String businessKey, String title) {
        Appointment appointment = new Appointment(id, businessKey,
                new AppointmentStatus.Requested(), title);
        try {
            appointments.insert(appointment);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateAppointment(businessKey, ex);
        }
        return appointment;
    }

    // Both INSERTs belong to this one transaction, entered through the Spring proxy.
    @Transactional
    public void insertTwice(String businessKey) {
        try {
            appointments.insert(new Appointment(AppointmentId.newId(), businessKey,
                    new AppointmentStatus.Requested(), "First appointment"));
            appointments.insert(new Appointment(AppointmentId.newId(), businessKey,
                    new AppointmentStatus.Requested(), "Duplicate appointment"));
        } catch (DuplicateKeyException ex) {
            throw new DuplicateAppointment(businessKey, ex);
        }
    }

    public int count(String businessKey) {
        return appointments.count(businessKey);
    }
}
