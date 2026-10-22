package kz.study.appointment.config;

import kz.study.appointment.domain.AppointmentPolicy;
import kz.study.appointment.domain.AppointmentRule;
import kz.study.appointment.domain.AppointmentTransitionRule;
import kz.study.appointment.domain.CompletedAppointmentCannotMoveRule;
import kz.study.appointment.domain.CompositeAppointmentRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppointmentRulesConfig {
    @Bean
    AppointmentRule appointmentRule() {
        return new CompositeAppointmentRule(
                new CompletedAppointmentCannotMoveRule(),
                new AppointmentTransitionRule(new AppointmentPolicy())
        );
    }
}