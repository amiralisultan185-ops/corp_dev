package kz.study.appointment.domain;

import java.util.Optional;

public interface AppointmentRepository {
    void insert(Appointment appointment);

    int count(String businessKey);

    Optional<Appointment> findByBusinessKey(String businessKey);
}
