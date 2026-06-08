package com.omarmujcic.timetracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeTrackingApplication.class, args);
    }
}
