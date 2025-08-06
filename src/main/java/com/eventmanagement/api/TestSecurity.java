package com.eventmanagement.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple test class to demonstrate how security would work in the Event Management API.
 * This class doesn't require Spring Security or JWT dependencies to compile.
 */
public class TestSecurity {
    
    // Simulated secret key for JWT signing
    private static final String SECRET_KEY = "simulated-secret-key-for-demonstration-purposes-only";
    
    // Simulated user database
    private static final Map<String, User> USERS = new HashMap<>();
    
    static {
        // Initialize with some test users
        USERS.put("admin@example.com", new User("1", "admin@example.com", "password123", "Admin", "User", "ADMIN"));
        USERS.put("organizer@example.com", new User("2", "organizer@example.com", "password123", "Organizer", "User", "ORGANIZER"));
        USERS.put("attendee@example.com", new User("3", "attendee@example.com", "password123", "Attendee", "User", "ATTENDEE"));
    }
    
    public static void main(String[] args) {
        System.out.println("=== Event Management API Security Test ===");
        
        // Test user registration
        User newUser = registerUser("john.doe@example.com", "password123", "John", "Doe", "ATTENDEE");
        System.out.println("Registered new user: " + newUser);
        
        // Test user login
        String adminToken = login("admin@example.com", "password123");
        System.out.println("Admin login successful, token: " + adminToken);
        
        String organizerToken = login("organizer@example.com", "password123");
        System.out.println("Organizer login successful, token: " + organizerToken);
        
        String attendeeToken = login("attendee@example.com", "password123");
        System.out.println("Attendee login successful, token: " + attendeeToken);
        
        // Test authorization
        System.out.println("\n=== Testing Authorization ===");
        
        // Admin should be able to access all endpoints
        testAuthorization("GET", "/api/users", adminToken); // Admin only
        testAuthorization("POST", "/api/events", adminToken); // Admin or Organizer
        testAuthorization("GET", "/api/events", adminToken); // All authenticated users
        
        // Organizer should be able to access some endpoints
        testAuthorization("GET", "/api/users", organizerToken); // Admin only - should fail
        testAuthorization("POST", "/api/events", organizerToken); // Admin or Organizer
        testAuthorization("GET", "/api/events", organizerToken); // All authenticated users
        
        // Attendee should have limited access
        testAuthorization("GET", "/api/users", attendeeToken); // Admin only - should fail
        testAuthorization("POST", "/api/events", attendeeToken); // Admin or Organizer - should fail
        testAuthorization("GET", "/api/events", attendeeToken); // All authenticated users
        
        System.out.println("\n=== Security Test Complete ===");
    }
    
    /**
     * Simulates user registration.
     */
    private static User registerUser(String email, String password, String firstName, String lastName, String role) {
        // Check if user already exists
        if (USERS.containsKey(email)) {
            System.out.println("Error: User with email " + email + " already exists");
            return null;
        }
        
        // Create new user
        String id = UUID.randomUUID().toString();
        User user = new User(id, email, password, firstName, lastName, role);
        USERS.put(email, user);
        return user;
    }
    
    /**
     * Simulates user login.
     */
    private static String login(String email, String password) {
        // Check if user exists
        User user = USERS.get(email);
        if (user == null) {
            System.out.println("Error: User with email " + email + " not found");
            return null;
        }
        
        // Check password
        if (!user.getPassword().equals(password)) {
            System.out.println("Error: Invalid password for user " + email);
            return null;
        }
        
        // Generate JWT token
        return generateToken(user);
    }
    
    /**
     * Simulates JWT token generation.
     */
    private static String generateToken(User user) {
        // In a real application, this would use a JWT library
        // This is a simplified simulation
        
        // Create header
        String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        
        // Create payload
        Instant now = Instant.now();
        Instant expiry = now.plus(24, ChronoUnit.HOURS);
        
        String payload = Base64.getEncoder().encodeToString((
            "{\"sub\":\"" + user.getId() + "\"," +
            "\"email\":\"" + user.getEmail() + "\"," +
            "\"roles\":[\"" + user.getRole() + "\"]," +
            "\"iat\":" + Date.from(now).getTime() / 1000 + "," +
            "\"exp\":" + Date.from(expiry).getTime() / 1000 + "}"
        ).getBytes());
        
        // In a real application, we would sign the token with the secret key
        // For simulation, we'll just use a placeholder signature
        String signature = Base64.getEncoder().encodeToString("simulated-signature".getBytes());
        
        return header + "." + payload + "." + signature;
    }
    
    /**
     * Simulates authorization check for an endpoint.
     */
    private static void testAuthorization(String method, String endpoint, String token) {
        if (token == null) {
            System.out.println(method + " " + endpoint + " - Unauthorized: No token provided");
            return;
        }
        
        // Parse token to get user role
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            System.out.println(method + " " + endpoint + " - Unauthorized: Invalid token format");
            return;
        }
        
        // In a real application, we would verify the signature
        // For simulation, we'll just decode the payload
        String payload = new String(Base64.getDecoder().decode(parts[1]));
        String role = payload.contains("ADMIN") ? "ADMIN" : 
                     payload.contains("ORGANIZER") ? "ORGANIZER" : 
                     payload.contains("ATTENDEE") ? "ATTENDEE" : "UNKNOWN";
        
        // Check authorization based on endpoint and role
        boolean authorized = false;
        
        if (endpoint.equals("/api/users") && method.equals("GET")) {
            // Admin only
            authorized = role.equals("ADMIN");
        } else if (endpoint.equals("/api/events") && method.equals("POST")) {
            // Admin or Organizer
            authorized = role.equals("ADMIN") || role.equals("ORGANIZER");
        } else if (endpoint.equals("/api/events") && method.equals("GET")) {
            // All authenticated users
            authorized = true;
        }
        
        if (authorized) {
            System.out.println(method + " " + endpoint + " - Authorized for role: " + role);
        } else {
            System.out.println(method + " " + endpoint + " - Forbidden: Role " + role + " not authorized");
        }
    }
    
    /**
     * Simple User class for security demonstration.
     */
    public static class User {
        private String id;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String role;
        
        public User(String id, String email, String password, String firstName, String lastName, String role) {
            this.id = id;
            this.email = email;
            this.password = password;
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
        
        public String getPassword() {
            return password;
        }
        
        public String getRole() {
            return role;
        }
        
        @Override
        public String toString() {
            return "User{id='" + id + "', email='" + email + "', firstName='" + firstName + 
                   "', lastName='" + lastName + "', role='" + role + "'}";
        }
    }
}