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
public class TestJWTComplete {
    
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
        simulateRequest("/api/events", "GET", organizerToken);
        simulateRequest("/api/events", "GET", attendeeToken);
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
                String expStr = payload.split("\"exp\":")[1].split("[,}]")[0].trim();
                long expTimestamp = Long.parseLong(expStr);
                long currentTimestamp = Instant.now().getEpochSecond();
                
                if (expTimestamp < currentTimestamp) {
                    System.out.println("Token validation failed: Token has expired");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Token validation failed: Error parsing expiration time");
                return;
            }
        }
        
        System.out.println("Token validation successful");
    }
    
    /**
     * Simulates an HTTP request with JWT authentication.
     */
    private static void simulateRequest(String endpoint, String method, String token) {
        System.out.println("\nRequest: " + method + " " + endpoint);
        System.out.println("Authorization: " + (token != null ? "Bearer " + token : "None"));
        
        // Simulate JWT filter
        if (token == null) {
            System.out.println("Response: 401 Unauthorized - No token provided");
            return;
        }
        
        // Validate token format
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            System.out.println("Response: 401 Unauthorized - Invalid token format");
            return;
        }
        
        // In a real application, we would verify the signature
        // For simulation, we'll just decode the payload
        String payload;
        try {
            payload = new String(Base64.getDecoder().decode(parts[1]));
        } catch (Exception e) {
            System.out.println("Response: 401 Unauthorized - Invalid token payload");
            return;
        }
        
        // Check if token is expired
        if (payload.contains("\"exp\":")) {
            try {
                String expStr = payload.split("\"exp\":")[1].split("[,}]")[0].trim();
                long expTimestamp = Long.parseLong(expStr);
                long currentTimestamp = Instant.now().getEpochSecond();
                
                if (expTimestamp < currentTimestamp) {
                    System.out.println("Response: 401 Unauthorized - Token has expired");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Response: 401 Unauthorized - Error parsing expiration time");
                return;
            }
        }
        
        // Extract user information from token
        String userId = "unknown";
        String userEmail = "unknown";
        String userRole = "unknown";
        
        // Simplified extraction using indexOf and substring
        if (payload.contains("\"sub\":")) {
            int start = payload.indexOf("\"sub\":\"");
            if (start >= 0) {
                start += 8; // Length of "\"sub\":\"" 
                int end = payload.indexOf("\"", start);
                if (end >= 0) {
                    userId = payload.substring(start, end);
                }
            }
        }
        
        // Extract email from payload
        if (payload.contains("\"email\":")) {
            int start = payload.indexOf("\"email\":\"");
            if (start >= 0) {
                start += 10; // Length of "\"email\":\"" 
                int end = payload.indexOf("\"", start);
                if (end >= 0) {
                    userEmail = payload.substring(start, end);
                }
            }
        }
        
        // Extract role from payload
        if (payload.contains("\"roles\":")) {
            int start = payload.indexOf("\"roles\":[\"\