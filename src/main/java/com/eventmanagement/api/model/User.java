package com.eventmanagement.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User document model for MongoDB.
 * Implements UserDetails for Spring Security integration.
 * Stores user authentication and profile information.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String firstName;
    
    private String lastName;
    
    private String phoneNumber;
    
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    
    @Builder.Default
    private boolean enabled = true;
    
    @Builder.Default
    private boolean accountNonExpired = true;
    
    @Builder.Default
    private boolean accountNonLocked = true;
    
    @Builder.Default
    private boolean credentialsNonExpired = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();
    
    /**
     * Returns the authorities granted to the user.
     * Converts roles to SimpleGrantedAuthority objects.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Embedded notification document for storing user notifications.
     * Using embedded document pattern for efficient retrieval.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification {
        private String id;
        private String title;
        private String message;
        private boolean read;
        private LocalDateTime createdAt;
        private String type; // e.g., "EVENT_REMINDER", "REGISTRATION_CONFIRMATION"
        private String relatedEntityId; // e.g., eventId or registrationId
    }
}