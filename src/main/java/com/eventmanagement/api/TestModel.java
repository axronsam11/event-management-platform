package com.eventmanagement.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple test model class to demonstrate the structure of the Event Management API models.
 * This class doesn't require Spring Boot or MongoDB dependencies to compile.
 */
public class TestModel {
    
    public static void main(String[] args) {
        System.out.println("=== Event Management API Model Test ===");
        
        // Create a test user
        User user = new User("1", "john.doe@example.com", "John", "Doe", "+1234567890");
        System.out.println("Created test user: " + user);
        
        // Create a test event
        Event event = new Event("1", "Tech Conference 2023", "Annual technology conference", 
                "New York", LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(32),
                "1", "PUBLISHED", "TECHNOLOGY");
        System.out.println("Created test event: " + event);
        
        // Add a speaker to the event
        event.addSpeaker(new Speaker("1", "Jane Smith", "CTO at Tech Corp", "https://example.com/jane.jpg"));
        System.out.println("Added speaker to event");
        
        // Add a ticket type to the event
        event.addTicketType(new TicketType("1", "VIP", "VIP access to all sessions", 199.99, 50, 0));
        System.out.println("Added ticket type to event");
        
        // Register the user for the event
        Registration registration = new Registration("1", "1", "1", "1", 199.99, "CONF123", LocalDateTime.now());
        System.out.println("Created registration: " + registration);
        
        System.out.println("\n=== Model Test Complete ===");
    }
    
    /**
     * Simple User class to demonstrate the structure.
     */
    public static class User {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        
        public User(String id, String email, String firstName, String lastName, String phoneNumber) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
        }
        
        @Override
        public String toString() {
            return "User{id='" + id + "', email='" + email + "', firstName='" + firstName + "', lastName='" + lastName + "'}";
        }
    }
    
    /**
     * Simple Event class to demonstrate the structure.
     */
    public static class Event {
        private String id;
        private String title;
        private String description;
        private String location;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String organizerId;
        private String status; // DRAFT, PUBLISHED, CANCELLED
        private String category;
        private List<Speaker> speakers = new ArrayList<>();
        private List<TicketType> ticketTypes = new ArrayList<>();
        
        public Event(String id, String title, String description, String location, 
                    LocalDateTime startDate, LocalDateTime endDate, String organizerId, 
                    String status, String category) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.location = location;
            this.startDate = startDate;
            this.endDate = endDate;
            this.organizerId = organizerId;
            this.status = status;
            this.category = category;
        }
        
        public void addSpeaker(Speaker speaker) {
            this.speakers.add(speaker);
        }
        
        public void addTicketType(TicketType ticketType) {
            this.ticketTypes.add(ticketType);
        }
        
        @Override
        public String toString() {
            return "Event{id='" + id + "', title='" + title + "', status='" + status + "', category='" + category + "'}";
        }
    }
    
    /**
     * Simple Speaker class to demonstrate the structure.
     */
    public static class Speaker {
        private String id;
        private String name;
        private String bio;
        private String imageUrl;
        
        public Speaker(String id, String name, String bio, String imageUrl) {
            this.id = id;
            this.name = name;
            this.bio = bio;
            this.imageUrl = imageUrl;
        }
    }
    
    /**
     * Simple TicketType class to demonstrate the structure.
     */
    public static class TicketType {
        private String id;
        private String name;
        private String description;
        private double price;
        private int quantity;
        private int sold;
        
        public TicketType(String id, String name, String description, double price, int quantity, int sold) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
            this.sold = sold;
        }
    }
    
    /**
     * Simple Registration class to demonstrate the structure.
     */
    public static class Registration {
        private String id;
        private String eventId;
        private String userId;
        private String ticketTypeId;
        private double amountPaid;
        private String confirmationCode;
        private LocalDateTime registrationDate;
        
        public Registration(String id, String eventId, String userId, String ticketTypeId, 
                           double amountPaid, String confirmationCode, LocalDateTime registrationDate) {
            this.id = id;
            this.eventId = eventId;
            this.userId = userId;
            this.ticketTypeId = ticketTypeId;
            this.amountPaid = amountPaid;
            this.confirmationCode = confirmationCode;
            this.registrationDate = registrationDate;
        }
        
        @Override
        public String toString() {
            return "Registration{id='" + id + "', eventId='" + eventId + "', userId='" + userId + 
                   "', confirmationCode='" + confirmationCode + "'}";
        }
    }
}