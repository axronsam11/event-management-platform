package com.eventmanagement.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple test class to demonstrate how JWT authentication would work in the Event Management API.
 * This class doesn't require Spring Security or JWT dependencies to compile.
 */
public class TestJWTComplete2 {
    
    // Simulated secret key for JWT signing
    private static final String SECRET_KEY = "simulated-secret-key-for-demonstration-purposes-only";
    
    // Simulated user database
    private static final Map<String, User> USERS = new HashMap<>();
    
    static {
        // Initialize with some test users
        USERS.put("1", new User("1", "admin@example.com", "Admin", "User", "ADMIN"));
        USERS.put("2", new User("2", "organizer@example.com", "Organizer", "User", "ORGANIZER"));
        USERS.put("3", new User("3", "attendee@example.com", "Attendee", "User", "ATTENDEE"));
    }
    
    public static void main(String[] args) {
        System.out.println("=== Event Management API JWT Authentication Test ===");
        
        // Generate JWT tokens for different users
        String adminToken = generateToken(USERS.get("1"));
        String organizerToken = generateToken(USERS.get("2"));
        String attendeeToken = generateToken(USERS.get("3"));
        
        System.out.println("\n=== Generated JWT Tokens ===");
        System.out.println("Admin Token: " + adminToken);
        System.out.println("Organizer Token: " + organizerToken);
        System.out.println("Attendee Token: " + attendeeToken);
        
        // Validate and parse tokens
        System.out.println("\n=== Validating JWT Tokens ===");
        validateToken(adminToken);
        validateToken(organizerToken);
        validateToken(attendeeToken);
        
        // Test token expiration
        System.out.println("\n=== Testing Token Expiration ===");
        String expiredToken = generateExpiredToken(USERS.get("1"));
        validateToken(expiredToken);
        
        // Test JWT filter
        System.out.println("\n=== Testing JWT Authentication Filter ===");
        simulateRequest("/api/events", "GET", adminToken);
        simulateRequest("/api/events", "POST", organizerToken);
        simulateRequest("/api/events", "DELETE", attendeeToken);
        simulateRequest("/api/admin/users", "GET", adminToken);
        simulateRequest("/api/admin/users", "GET", organizerToken);
        simulateRequest("/api/users/profile", "GET", attendeeToken);
        simulateRequest("/api/events", "GET", "invalid-token");
        simulateRequest("/api/events", "GET", null);
        
        System.out.println("\n=== JWT Authentication Test Complete ===");
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
     * Simulates generating an expired JWT token.
     */
    private static String generateExpiredToken(User user) {
        // Create header
        String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        
        // Create payload with expired timestamp
        Instant now = Instant.now();
        Instant expiry = now.minus(1, ChronoUnit.HOURS); // Expired 1 hour ago
        
        String payload = Base64.getEncoder().encodeToString((
            "{\"sub\":\"" + user.getId() + "\"," +
            "\"email\":\"" + user.getEmail() + "\"," +
            "\"roles\":[\"" + user.getRole() + "\"]," +
            "\"iat\":" + Date.from(now.minus(2, ChronoUnit.HOURS)).getTime() / 1000 + "," +
            "\"exp\":" + Date.from(expiry).getTime() / 1000 + "}"
        ).getBytes());
        
        String signature = Base64.getEncoder().encodeToString("simulated-signature".getBytes());
        
        return header + "." + payload + "." + signature;
    }
    
    /**
     * Simulates JWT token validation.
     */
    private static void validateToken(String token) {
        if (token == null) {
            System.out.println("Token validation failed: Token is null");
            return;
        }
        
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            System.out.println("Token validation failed: Invalid token format");
            return;
        }
        
        // In a real application, we would verify the signature
        // For simulation, we'll just decode the payload
        String payload = new String(Base64.getDecoder().decode(parts[1]));
        System.out.println("Token payload: " + payload);
        
        // Check if token is expired
        if (payload.contains("\"exp\":")) {
            try {
                String expStr = payload.split("\"exp\":