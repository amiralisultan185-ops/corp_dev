package kz.study.appointment;
import kz.study.appointment.domain.AppointmentPolicy;
import kz.study.appointment.domain.AppointmentStatus;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentPolicyTest {

    private AppointmentStatus statusOf(String name) {
        return switch (name) {
            case "Requested" -> new AppointmentStatus.Requested();
            case "Confirmed" -> new AppointmentStatus.Confirmed();
            case "Completed" -> new AppointmentStatus.Completed();
            default -> throw new IllegalArgumentException(
                    "Unknown status: " + name
            );
        };
    }

    @ParameterizedTest
    @CsvSource({
            "Requested, Confirmed, true",
            "Confirmed, Completed, true",
            "Requested, Completed, false",
            "Completed, Requested, false"
    })
    void checksTransition(String fromName, String toName, boolean allowed) {
        AppointmentStatus from = statusOf(fromName);
        AppointmentStatus to = statusOf(toName);
        AppointmentPolicy policy = new AppointmentPolicy();

        if (allowed) {
            AppointmentStatus result = policy.move(from, to);
            assertEquals(to, result);
        } else {
            assertThrows(
                    IllegalStateException.class,
                    () -> policy.move(from, to)
            );
        }
    }
}