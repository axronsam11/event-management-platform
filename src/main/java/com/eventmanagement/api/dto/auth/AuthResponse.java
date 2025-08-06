package com.eventmanagement.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for authentication responses.
 * Contains JWT token and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;

    /**
     * Static factory method to create an AuthResponse with a Bearer token.
     *
     * @param token The JWT token
     * @param userId The user ID
     * @param email The user email
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param roles The user's roles
     * @return An AuthResponse object
     */
    public static AuthResponse withBearerToken(String token, String userId, String email, 
                                              String firstName, String lastName, List<String> roles) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .build();
    }
}