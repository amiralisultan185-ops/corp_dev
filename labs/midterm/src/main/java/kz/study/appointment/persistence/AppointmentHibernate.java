package kz.study.appointment.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import kz.study.appointment.domain.Appointment;
import kz.study.appointment.domain.AppointmentRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class AppointmentHibernate implements AppointmentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void insert(Appointment appointment) {
        try {
            // persist keeps the Lab 3 INSERT semantics, including duplicate IDs.
            entityManager.persist(AppointmentEntity.fromDomain(appointment));
            // Execute SQL inside this method so the service can catch the duplicate.
            entityManager.flush();
        } catch (PersistenceException error) {
            for (Throwable cause = error; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "23505".equals(violation.getSQLState())) {
                    throw new DuplicateKeyException("Duplicate appointment: " + appointment.businessKey(), error);
                }
            }
            throw error;
        }
    }

    @Override
    public int count(String businessKey) {
        Long count = entityManager.createQuery(
                        "select count(a) from AppointmentEntity a where a.businessKey = :key", Long.class)
                .setParameter("key", businessKey)
                .getSingleResult();
        return Math.toIntExact(count);
    }

    @Override
    public Optional<Appointment> findByBusinessKey(String businessKey) {
        return entityManager.createQuery(
                        "select a from AppointmentEntity a where a.businessKey = :key", AppointmentEntity.class)
                .setParameter("key", businessKey)
                .getResultList().stream()
                .findFirst()
                .map(AppointmentEntity::toDomain);
    }
}
