package com.eventmanagement.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Main application class for the Event Management Platform.
 * This application provides a secure and scalable solution for managing events,
 * users, registrations, and communications using Spring Boot and MongoDB Atlas.
 */
@SpringBootApplication
@EnableMongoAuditing
public class EventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
    }
}