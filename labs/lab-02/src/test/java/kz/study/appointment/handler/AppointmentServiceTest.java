package kz.study.appointment.handler;

import kz.study.appointment.config.Application;
import kz.study.appointment.domain.AppointmentId;
import kz.study.appointment.domain.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Application.class)
class AppointmentServiceTest {
    @Autowired
    private AppointmentService service;

    @Test
    void springInjectsAppointmentRuleIntoService() {
        AppointmentStatus result = service.move(
                new AppointmentId("apt-001"),
                new AppointmentStatus.Requested(),
                new AppointmentStatus.Confirmed()
        );

        assertEquals(new AppointmentStatus.Confirmed(), result);
    }
}