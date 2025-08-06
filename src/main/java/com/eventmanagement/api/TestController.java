package com.eventmanagement.api;

/**
 * Simple test controller to verify the project structure.
 * This is just for testing purposes and doesn't require Spring Boot to compile.
 */
public class TestController {
    
    /**
     * Simulates a controller endpoint that would return a greeting message.
     * 
     * @param name The name to greet
     * @return A greeting message
     */
    public String greet(String name) {
        return "Hello, " + name + "! Welcome to the Event Management API.";
    }
    
    /**
     * Simulates a controller endpoint that would return information about the API.
     * 
     * @return Information about the API
     */
    public String getApiInfo() {
        return "Event Management API - A comprehensive RESTful API for event management\n" +
               "Built with Spring Boot and MongoDB\n" +
               "Features:\n" +
               "- User Management\n" +
               "- Event Management\n" +
               "- Event Registration\n" +
               "- Notifications\n" +
               "- Role-Based Access Control";
    }
}