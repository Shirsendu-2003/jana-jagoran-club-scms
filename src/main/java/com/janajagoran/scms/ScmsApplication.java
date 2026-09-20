package com.janajagoran.scms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Jana Jagoran Club - Society Club Management System (SCMS)
 * Main application entry point.
 */
@SpringBootApplication
@EnableScheduling
public class ScmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScmsApplication.class, args);
    }
}
