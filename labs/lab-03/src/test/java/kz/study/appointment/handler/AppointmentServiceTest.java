package kz.study.appointment.handler;

import kz.study.appointment.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// A plain unit test: no Spring context, database, or real notification endpoint.
class AppointmentServiceTest {
    private AppointmentNotifier notifier;
    private AppointmentRepository appointments;
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        notifier = mock(AppointmentNotifier.class);
        appointments = mock(AppointmentRepository.class);
        AppointmentRule rules = new CompositeAppointmentRule(
                new CompletedAppointmentCannotMoveRule(),
                new AppointmentTransitionRule(new AppointmentPolicy()));
        service = new AppointmentService(rules, notifier, appointments);
    }

    @Test
    void allowedMoveNotifiesOutboundPort() {
        AppointmentId id = AppointmentId.newId();
        AppointmentStatus target = new AppointmentStatus.Confirmed();

        AppointmentStatus result = service.move(id, new AppointmentStatus.Requested(), target);

        assertEquals(target, result);
        verify(notifier).statusChanged(id, target);
        verifyNoMoreInteractions(notifier);
        verifyNoInteractions(appointments);
    }

    @Test
    void forbiddenMoveDoesNotNotify() {
        assertThrows(IllegalStateException.class, () -> service.move(
                AppointmentId.newId(), new AppointmentStatus.Requested(),
                new AppointmentStatus.Completed()));

        verifyNoInteractions(notifier, appointments);
    }
}
