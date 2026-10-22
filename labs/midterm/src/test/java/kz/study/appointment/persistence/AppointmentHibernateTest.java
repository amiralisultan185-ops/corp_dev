package kz.study.appointment.persistence;

import kz.study.appointment.config.Application;
import kz.study.appointment.domain.*;
import kz.study.appointment.handler.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.flywaydb.core.Flyway;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
// Intentionally NO @Transactional: count must observe the committed outcome.
class AppointmentHibernateTest {
    @Autowired private AppointmentService service;
    @Autowired private AppointmentHibernate appointments;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private Flyway flyway;

    @BeforeEach
    void clearTestRows() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals("appointment_midterm_test",
                jdbc.queryForObject("SELECT current_schema()", String.class));
        jdbc.execute("DELETE FROM appointment_midterm_test.appointment");
    }

    @Test
    void secondStatementRollsBack() {
        DuplicateAppointment error = assertThrows(DuplicateAppointment.class,
                () -> service.insertTwice("APT-1042"));

        assertSqlState("23505", error);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals(0, appointments.count("APT-1042"));
        assertTrue(appointments.findByBusinessKey("APT-1042").isEmpty());
    }

    @Test
    void secondRequestKeepsTheFirst() {
        Appointment first = service.register(AppointmentId.newId(), "APT-1042", "First visit");

        assertThrows(DuplicateAppointment.class,
                () -> service.register(AppointmentId.newId(), "APT-1042", "Duplicate visit"));

        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals(1, service.count("APT-1042"));
        assertEquals(first, appointments.findByBusinessKey("APT-1042").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"REQUESTED", "CONFIRMED", "COMPLETED"})
    void roundTripsEverySealedStatus(String status) {
        Appointment appointment = new Appointment(AppointmentId.newId(), "APT-STATUS",
                AppointmentEntity.statusFromText(status), "Follow-up visit");
        appointments.insert(appointment);

        assertEquals(appointment, appointments.findByBusinessKey("APT-STATUS").orElseThrow());
        assertNotNull(jdbc.queryForObject(
                "SELECT created_at FROM appointment WHERE business_key = ?",
                Object.class, "APT-STATUS"));
    }

    @Test
    void postgresRejectsUnknownStatus() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        INSERT INTO appointment (id, business_key, status, title)
                        VALUES (?, ?, ?, ?)
                        """, UUID.randomUUID(), "APT-BAD", "UNKNOWN", "Invalid status"));
        assertSqlState("23514", error);
        assertEquals(0, appointments.count("APT-BAD"));
    }

    @Test
    void postgresRejectsNullBusinessKey() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        INSERT INTO appointment (id, business_key, status, title)
                        VALUES (?, ?, ?, ?)
                        """, UUID.randomUUID(), null, "REQUESTED", "Missing key"));
        assertSqlState("23502", error);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void javaRejectsBlankBusinessKey(String key) {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(AppointmentId.newId(), key, "Visit"));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM appointment", Integer.class));
    }

    @Test
    void placeholdersPreserveQuotedInput() {
        String key = "APT-' OR 1=1 --";
        Appointment saved = service.register(AppointmentId.newId(), key, "Patient's visit");

        assertEquals(1, appointments.count(key));
        assertEquals(0, appointments.count("APT-other"));
        assertEquals(saved, appointments.findByBusinessKey(key).orElseThrow());
    }

    @Test
    void mapperRejectsUnknownDatabaseText() {
        assertThrows(IllegalArgumentException.class,
                () -> AppointmentEntity.statusFromText("UNKNOWN"));
    }

    @Test
    void flywayMigrationIsRecordedAndDoesNotRepeat() {
        Appointment first = service.register(AppointmentId.newId(), "APT-MIGRATION", "Keep this visit");
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM flyway_schema_history
                WHERE version = '1' AND script = 'V1__create_appointment.sql' AND success
                """, Integer.class));
        flyway.validate();
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(first, appointments.findByBusinessKey("APT-MIGRATION").orElseThrow());
    }

    @Test
    void duplicateIdDoesNotOverwriteOriginal() {
        AppointmentId id = AppointmentId.newId();
        Appointment first = service.register(id, "APT-ORIGINAL", "Original visit");
        assertThrows(DuplicateAppointment.class,
                () -> service.register(id, "APT-OTHER", "Replacement visit"));
        assertEquals(first, appointments.findByBusinessKey("APT-ORIGINAL").orElseThrow());
        assertEquals(0, appointments.count("APT-OTHER"));
    }
    private static void assertSqlState(String expected, Throwable failure) {
        Throwable cause = failure;
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }
        assertInstanceOf(SQLException.class, cause);
        assertEquals(expected, ((SQLException) cause).getSQLState());
    }
}
