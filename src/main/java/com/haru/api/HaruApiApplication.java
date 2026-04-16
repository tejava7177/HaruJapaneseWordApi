package com.haru.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HaruApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HaruApiApplication.class, args);
    }
}
