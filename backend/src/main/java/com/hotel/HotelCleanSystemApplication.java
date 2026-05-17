package com.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelCleanSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelCleanSystemApplication.class, args);
    }
}
