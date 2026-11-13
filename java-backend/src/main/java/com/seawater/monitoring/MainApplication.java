package com.seawater.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sea Water Monitoring System - Spring Boot IoT Application
 * Communicates directly with Blynk IoT Cloud REST API to fetch live sensor readings.
 */
@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" SEA WATER MONITORING SYSTEM - SPRING BOOT SERVER STARTED        ");
        System.out.println(" Dashboard URL: http://localhost:8080                            ");
        System.out.println(" REST API:      http://localhost:8080/api/sensors                ");
        System.out.println(" Blynk Status:  http://localhost:8080/api/blynk/status           ");
        System.out.println("==================================================================");
    }
}

