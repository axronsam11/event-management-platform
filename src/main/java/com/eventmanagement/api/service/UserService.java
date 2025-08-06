package com.eventmanagement.api.service;

import com.eventmanagement.api.dto.user.NotificationResponse;
import com.eventmanagement.api.dto.user.UserProfileRequest;
import com.eventmanagement.api.dto.user.UserProfileResponse;
import com.eventmanagement.api.exception.ResourceNotFoundException;
import com.eventmanagement.api.model.User;
import com.eventmanagement.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling user operations.
 * Manages user profiles, notifications, and admin functions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get the current authenticated user.
     *
     * @return The current user entity
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    /**
     * Get a user by ID.
     *
     * @param userId The ID of the user to retrieve
     * @return The user entity
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Get the current user's profile.
     *
     * @return The user profile response DTO
     */
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return mapToUserProfileResponse(user);
    }

    /**
     * Get a user's profile by ID.
     *
     * @param userId The ID of the user to retrieve
     * @return The user profile response DTO
     */
    public UserProfileResponse getUserProfile(String userId) {
        User user = getUserById(userId);
        return mapToUserProfileResponse(user);
    }

    /**
     * Update the current user's profile.
     *
     * @param profileRequest The user profile request DTO
     * @return The updated user profile response DTO
     */
    public UserProfileResponse updateCurrentUserProfile(UserProfileRequest profileRequest) {
        User currentUser = getCurrentUser();
        
        // Update user fields
        currentUser.setFirstName(profileRequest.getFirstName());
        currentUser.setLastName(profileRequest.getLastName());
        currentUser.setPhoneNumber(profileRequest.getPhoneNumber());
        
        // Only update password if provided
        if (profileRequest.getPassword() != null && !profileRequest.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(profileRequest.getPassword()));
        }
        
        User updatedUser = userRepository.save(currentUser);
        return mapToUserProfileResponse(updatedUser);
    }

    /**
     * Update a user's profile by ID (admin only).
     *
     * @param userId The ID of the user to update
     * @param profileRequest The user profile request DTO
     * @return The updated user profile response DTO
     */
    public UserProfileResponse updateUserProfile(String userId, UserProfileRequest profileRequest) {
        User currentUser = getCurrentUser();
        
        // Check if user is an admin
        if (!currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can update other users' profiles");
        }
        
        User userToUpdate = getUserById(userId);
        
        // Update user fields
        userToUpdate.setFirstName(profileRequest.getFirstName());
        userToUpdate.setLastName(profileRequest.getLastName());
        userToUpdate.setPhoneNumber(profileRequest.getPhoneNumber());
        userToUpdate.setEnabled(profileRequest.isEnabled());
        
        // Update roles if provided
        if (profileRequest.getRoles() != null && !profileRequest.getRoles().isEmpty()) {
            userToUpdate.setRoles(profileRequest.getRoles());
        }
        
        // Only update password if provided
        if (profileRequest.getPassword() != null && !profileRequest.getPassword().isEmpty()) {
            userToUpdate.setPassword(passwordEncoder.encode(profileRequest.getPassword()));
        }
        
        User updatedUser = userRepository.save(userToUpdate);
        return mapToUserProfileResponse(updatedUser);
    }

    /**
     * Delete a user by ID (admin only).
     *
     * @param userId The ID of the user to delete
     */
    public void deleteUser(String userId) {
        User currentUser = getCurrentUser();
        
        // Check if user is an admin
        if (!currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can delete users");
        }
        
        User userToDelete = getUserById(userId);
        userRepository.delete(userToDelete);
    }

    /**
     * Get all users with pagination (admin only).
     *
     * @param pageable Pagination information
     * @return Page of user profile response DTOs
     */
    public Page<UserProfileResponse> getAllUsers(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        // Check if user is an admin
        if (!currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can view all users");
        }
        
        return userRepository.findAll(pageable)
                .map(this::mapToUserProfileResponse);
    }

    /**
     * Search users by name with pagination (admin only).
     *
     * @param name The name to search for
     * @param pageable Pagination information
     * @return Page of user profile response DTOs
     */
    public Page<UserProfileResponse> searchUsersByName(String name, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        // Check if user is an admin
        if (!currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can search users");
        }
        
        return userRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::mapToUserProfileResponse);
    }

    /**
     * Get users by role with pagination (admin only).
     *
     * @param role The role to filter by
     * @param pageable Pagination information
     * @return Page of user profile response DTOs
     */
    public Page<UserProfileResponse> getUsersByRole(String role, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        // Check if user is an admin
        if (!currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can filter users by role");
        }
        
        return userRepository.findByRolesContaining(role, pageable)
                .map(this::mapToUserProfileResponse);
    }

    /**
     * Get the current user's notifications.
     *
     * @return List of notification response DTOs
     */
    public List<NotificationResponse> getCurrentUserNotifications() {
        User currentUser = getCurrentUser();
        return mapToNotificationResponses(currentUser.getNotifications());
    }

    /**
     * Get the current user's unread notifications.
     *
     * @return List of unread notification response DTOs
     */
    public List<NotificationResponse> getCurrentUserUnreadNotifications() {
        User currentUser = getCurrentUser();
        return currentUser.getNotifications().stream()
                .filter(notification -> !notification.isRead())
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark a notification as read.
     *
     * @param notificationId The ID of the notification to mark as read
     * @return The updated notification response DTO
     */
    public NotificationResponse markNotificationAsRead(String notificationId) {
        User currentUser = getCurrentUser();
        
        User.Notification notification = currentUser.getNotifications().stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        
        notification.setRead(true);
        User updatedUser = userRepository.save(currentUser);
        
        return updatedUser.getNotifications().stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .map(this::mapToNotificationResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
    }

    /**
     * Mark all notifications as read for the current user.
     */
    public void markAllNotificationsAsRead() {
        User currentUser = getCurrentUser();
        
        currentUser.getNotifications().forEach(notification -> notification.setRead(true));
        userRepository.save(currentUser);
    }

    /**
     * Create a notification for a user.
     *
     * @param userId The ID of the user to notify
     * @param title The notification title
     * @param message The notification message
     * @param type The notification type
     * @param relatedEntityId The ID of the related entity (optional)
     * @return The created notification response DTO
     */
    public NotificationResponse createNotification(String userId, String title, String message, 
                                                  String type, String relatedEntityId) {
        User user = getUserById(userId);
        
        User.Notification notification = User.Notification.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .type(type)
                .relatedEntityId(relatedEntityId)
                .build();
        
        user.getNotifications().add(notification);
        User updatedUser = userRepository.save(user);
        
        return updatedUser.getNotifications().stream()
                .filter(n -> n.getId().equals(notification.getId()))
                .findFirst()
                .map(this::mapToNotificationResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notification.getId()));
    }

    /**
     * Delete a notification.
     *
     * @param notificationId The ID of the notification to delete
     */
    public void deleteNotification(String notificationId) {
        User currentUser = getCurrentUser();
        
        boolean removed = currentUser.getNotifications().removeIf(n -> n.getId().equals(notificationId));
        
        if (!removed) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        
        userRepository.save(currentUser);
    }

    /**
     * Map a user entity to a user profile response DTO.
     *
     * @param user The user entity
     * @return The user profile response DTO
     */
    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Map a list of notification entities to notification response DTOs.
     *
     * @param notifications The list of notification entities
     * @return List of notification response DTOs
     */
    private List<NotificationResponse> mapToNotificationResponses(List<User.Notification> notifications) {
        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map a notification entity to a notification response DTO.
     *
     * @param notification The notification entity
     * @return The notification response DTO
     */
    private NotificationResponse mapToNotificationResponse(User.Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .type(notification.getType())
                .relatedEntityId(notification.getRelatedEntityId())
                .build();
    }
}