package com.slotfy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlotfyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlotfyApplication.class, args);
    }
}