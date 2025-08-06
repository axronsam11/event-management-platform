package com.eventmanagement.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Simple test class to demonstrate how the API endpoints would work in the Event Management API.
 * This class doesn't require Spring Boot or MongoDB dependencies to compile.
 */
public class TestAPI {
    
    // Simulated databases
    private static final Map<String, User> USERS = new HashMap<>();
    private static final Map<String, Event> EVENTS = new HashMap<>();
    private static final Map<String, Registration> REGISTRATIONS = new HashMap<>();
    private static final Map<String, List<Notification>> USER_NOTIFICATIONS = new HashMap<>();
    
    static {
        // Initialize with some test data
        User admin = new User("1", "admin@example.com", "Admin", "User", "ADMIN");
        User organizer = new User("2", "organizer@example.com", "Organizer", "User", "ORGANIZER");
        User attendee = new User("3", "attendee@example.com", "Attendee", "User", "ATTENDEE");
        
        USERS.put(admin.getId(), admin);
        USERS.put(organizer.getId(), organizer);
        USERS.put(attendee.getId(), attendee);
        
        Event event1 = new Event("1", "Tech Conference 2023", "Annual technology conference", 
                "PUBLISHED", "TECHNOLOGY", "New York", new Date(), new Date());
        Event event2 = new Event("2", "Music Festival 2023", "Annual music festival", 
                "PUBLISHED", "ENTERTAINMENT", "Los Angeles", new Date(), new Date());
        
        EVENTS.put(event1.getId(), event1);
        EVENTS.put(event2.getId(), event2);
        
        Registration reg1 = new Registration("1", event1.getId(), attendee.getId(), "CONF123");
        REGISTRATIONS.put(reg1.getId(), reg1);
        
        List<Notification> adminNotifications = new ArrayList<>();
        adminNotifications.add(new Notification("1", admin.getId(), "System Update", "System will be down for maintenance", false));
        USER_NOTIFICATIONS.put(admin.getId(), adminNotifications);
        
        List<Notification> attendeeNotifications = new ArrayList<>();
        attendeeNotifications.add(new Notification("2", attendee.getId(), "Event Reminder", "Tech Conference starts tomorrow", false));
        USER_NOTIFICATIONS.put(attendee.getId(), attendeeNotifications);
    }
    
    public static void main(String[] args) {
        System.out.println("=== Event Management API Test ===");
        
        // Test authentication endpoints
        System.out.println("\n=== Testing Auth Controller ===");
        testAuthController();
        
        // Test event endpoints
        System.out.println("\n=== Testing Event Controller ===");
        testEventController();
        
        // Test user endpoints
        System.out.println("\n=== Testing User Controller ===");
        testUserController();
        
        System.out.println("\n=== API Test Complete ===");
    }
    
    /**
     * Tests the AuthController endpoints.
     */
    private static void testAuthController() {
        // Test registration
        System.out.println("POST /api/auth/register - Registering new user");
        User newUser = new User(UUID.randomUUID().toString(), "jane.doe@example.com", "Jane", "Doe", "ATTENDEE");
        USERS.put(newUser.getId(), newUser);
        System.out.println("Response: User registered successfully - " + newUser);
        
        // Test login
        System.out.println("\nPOST /api/auth/login - Logging in user");
        String token = "simulated-jwt-token-for-" + newUser.getEmail();
        System.out.println("Response: Login successful - Token: " + token);
    }
    
    /**
     * Tests the EventController endpoints.
     */
    private static void testEventController() {
        // Test get all events
        System.out.println("GET /api/events - Getting all events");
        System.out.println("Response: " + EVENTS.values());
        
        // Test get event by ID
        String eventId = "1";
        System.out.println("\nGET /api/events/" + eventId + " - Getting event by ID");
        Event event = EVENTS.get(eventId);
        System.out.println("Response: " + event);
        
        // Test create event
        System.out.println("\nPOST /api/events - Creating new event");
        Event newEvent = new Event(
            UUID.randomUUID().toString(),
            "Workshop 2023",
            "Hands-on workshop",
            "DRAFT",
            "EDUCATION",
            "Chicago",
            new Date(),
            new Date()
        );
        EVENTS.put(newEvent.getId(), newEvent);
        System.out.println("Response: Event created successfully - " + newEvent);
        
        // Test update event
        System.out.println("\nPUT /api/events/" + newEvent.getId() + " - Updating event");
        newEvent.setStatus("PUBLISHED");
        System.out.println("Response: Event updated successfully - " + newEvent);
        
        // Test delete event
        System.out.println("\nDELETE /api/events/" + newEvent.getId() + " - Deleting event");
        EVENTS.remove(newEvent.getId());
        System.out.println("Response: Event deleted successfully");
        
        // Test register for event
        System.out.println("\nPOST /api/events/" + eventId + "/register - Registering for event");
        String userId = "3"; // Attendee
        Registration registration = new Registration(
            UUID.randomUUID().toString(),
            eventId,
            userId,
            "CONF" + UUID.randomUUID().toString().substring(0, 8)
        );
        REGISTRATIONS.put(registration.getId(), registration);
        System.out.println("Response: Registration successful - " + registration);
    }
    
    /**
     * Tests the UserController endpoints.
     */
    private static void testUserController() {
        // Test get current user profile
        String userId = "3"; // Attendee
        System.out.println("GET /api/users/me - Getting current user profile");
        User user = USERS.get(userId);
        System.out.println("Response: " + user);
        
        // Test update current user profile
        System.out.println("\nPUT /api/users/me - Updating current user profile");
        user.setFirstName("Updated");
        user.setLastName("Name");
        System.out.println("Response: Profile updated successfully - " + user);
        
        // Test get all users (admin only)
        System.out.println("\nGET /api/users - Getting all users (admin only)");
        System.out.println("Response: " + USERS.values());
        
        // Test get user by ID (admin only)
        System.out.println("\nGET /api/users/" + userId + " - Getting user by ID (admin only)");
        System.out.println("Response: " + user);
        
        // Test get user notifications
        System.out.println("\nGET /api/users/" + userId + "/notifications - Getting user notifications");
        List<Notification> notifications = USER_NOTIFICATIONS.getOrDefault(userId, new ArrayList<>());
        System.out.println("Response: " + notifications);
        
        // Test create notification (admin only)
        System.out.println("\nPOST /api/users/" + userId + "/notifications - Creating notification (admin only)");
        Notification newNotification = new Notification(
            UUID.randomUUID().toString(),
            userId,
            "New Feature",
            "Check out our new feature",
            false
        );
        notifications.add(newNotification);
        USER_NOTIFICATIONS.put(userId, notifications);
        System.out.println("Response: Notification created successfully - " + newNotification);
        
        // Test mark notification as read
        String notificationId = newNotification.getId();
        System.out.println("\nPUT /api/users/" + userId + "/notifications/" + notificationId + "/read - Marking notification as read");
        newNotification.setRead(true);
        System.out.println("Response: Notification marked as read - " + newNotification);
    }
    
    /**
     * Simple User class for API demonstration.
     */
    public static class User {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        
        public User(String id, String email, String firstName, String lastName, String role) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
        }
        
        public String getId() {
            return id;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        @Override
        public String toString() {
            return "User{id='" + id + "', email='" + email + "', firstName='" + firstName + 
                   "', lastName='" + lastName + "', role='" + role + "'}";
        }
    }
    
    /**
     * Simple Event class for API demonstration.
     */
    public static class Event {
        private String id;
        private String title;
        private String description;
        private String status;
        private String category;
        private String location;
        private Date startDate;
        private Date endDate;
        
        public Event(String id, String title, String description, String status, String category, 
                     String location, Date startDate, Date endDate) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.status = status;
            this.category = category;
            this.location = location;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public String getId() {
            return id;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return "Event{id='" + id + "', title='" + title + "', status='" + status + 
                   "', category='" + category + "', location='" + location + 
                   "', startDate='" + sdf.format(startDate) + "', endDate='" + sdf.format(endDate) + "'}";
        }
    }
    
    /**
     * Simple Registration class for API demonstration.
     */
    public static class Registration {
        private String id;
        private String eventId;
        private String userId;
        private String confirmationCode;
        
        public Registration(String id, String eventId, String userId, String confirmationCode) {
            this.id = id;
            this.eventId = eventId;
            this.userId = userId;
            this.confirmationCode = confirmationCode;
        }
        
        public String getId() {
            return id;
        }
        
        @Override
        public String toString() {
            return "Registration{id='" + id + "', eventId='" + eventId + "', userId='" + userId + 
                   "', confirmationCode='" + confirmationCode + "'}";
        }
    }
    
    /**
     * Simple Notification class for API demonstration.
     */
    public static class Notification {
        private String id;
        private String userId;
        private String title;
        private String message;
        private boolean read;
        
        public Notification(String id, String userId, String title, String message, boolean read) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.read = read;
        }
        
        public String getId() {
            return id;
        }
        
        public void setRead(boolean read) {
            this.read = read;
        }
        
        @Override
        public String toString() {
            return "Notification{id='" + id + "', userId='" + userId + "', title='" + title + 
                   "', message='" + message + "', read=" + read + "}";
        }
    }
}