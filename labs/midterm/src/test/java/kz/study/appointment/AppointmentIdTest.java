package kz.study.appointment;

import kz.study.appointment.domain.AppointmentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentIdTest {
    @Test
    void keepsValidValue() {
        UUID value = UUID.fromString("11111111-1111-1111-1111-111111111111");
        assertEquals(value, AppointmentId.parse(value.toString()).value());
    }

    @Test
    void generatesSurrogateIds() {
        AppointmentId first = AppointmentId.newId();
        assertNotNull(first.value());
        assertNotEquals(first, AppointmentId.newId());
    }

    @Test
    void rejectsNullUuid() {
        assertThrows(IllegalArgumentException.class, () -> new AppointmentId(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "apt-001", "1-1-1-1-1"})
    void rejectsMissingOrMalformedId(String value) {
        assertThrows(IllegalArgumentException.class, () -> AppointmentId.parse(value));
    }
}
