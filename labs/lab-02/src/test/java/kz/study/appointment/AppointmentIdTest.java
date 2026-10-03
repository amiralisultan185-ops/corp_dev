package kz.study.appointment;
import kz.study.appointment.domain.AppointmentId;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentIdTest {

    @Test
    void keepsValidValue() {
        AppointmentId id = new AppointmentId("apt-001");

        assertEquals("apt-001", id.value());
    }

    @Test
    void rejectsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AppointmentId(null)
        );
    }

    @Test
    void rejectsEmptyValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AppointmentId("")
        );
    }

    @Test
    void rejectsBlankValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AppointmentId("   ")
        );
    }
}