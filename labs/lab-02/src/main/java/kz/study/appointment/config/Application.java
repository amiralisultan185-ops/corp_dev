package kz.study.appointment.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "kz.study.appointment")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
