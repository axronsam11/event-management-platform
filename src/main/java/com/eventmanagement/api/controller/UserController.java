package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.user.NotificationResponse;
import com.eventmanagement.api.dto.user.UserProfileRequest;
import com.eventmanagement.api.dto.user.UserProfileResponse;
import com.eventmanagement.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user operations.
 * Provides endpoints for managing user profiles and notifications.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Get the current user's profile.
     *
     * @return The user profile response DTO
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the profile of the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    /**
     * Update the current user's profile.
     *
     * @param profileRequest The user profile request DTO
     * @return The updated user profile response DTO
     */
    @PutMapping("/me")
    @Operation(
            summary = "Update current user profile",
            description = "Updates the profile of the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(@Valid @RequestBody UserProfileRequest profileRequest) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(profileRequest));
    }

    /**
     * Get a user's profile by ID (admin only).
     *
     * @param userId The ID of the user to retrieve
     * @return The user profile response DTO
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user profile by ID",
            description = "Retrieves a user's profile by ID. Only accessible by admins.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(description = "ID of the user to retrieve") @PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    /**
     * Update a user's profile by ID (admin only).
     *
     * @param userId The ID of the user to update
     * @param profileRequest The user profile request DTO
     * @return The updated user profile response DTO
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user profile by ID",
            description = "Updates a user's profile by ID. Only accessible by admins.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Parameter(description = "ID of the user to update") @PathVariable String userId,
            @Valid @RequestBody UserProfileRequest profileRequest) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, profileRequest));
    }

    /**
     * Delete a user by ID (admin only).
     *
     * @param userId The ID of the user to delete
     * @return No content response
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user by ID",
            description = "Deletes a user by ID. Only accessible by admins.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all users with pagination (admin only).
     *
     * @param pageable Pagination information
     * @return Page of user profile response DTOs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Retrieves all users with pagination. Only accessible by admins.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<Page<UserProfileResponse>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    /**
     * Search users by name with pagination (admin only).
     *
     * @param name The name to search for
     * @param pageable Pagination information
     * @return Page of user profile response DTOs
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Search users by name",
            description = "Searches users by name with pagination. Only accessible by admins.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<Page<UserProfileResponse>> searchUsersByName(
            @Parameter(description = "Name to search for") @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsersByName(name, pageable));
    }

    /**
     * Get users by role with pagination (admin only).
     *
     * @param role The role to filter by
     * @param pageable Pagination information
     * @return Page of user profile response DTOs
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get users by role",
            description = "Retrieves users by role with pagination. Only accessible by admins.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<Page<UserProfileResponse>> getUsersByRole(
            @Parameter(description = "Role to filter by") @PathVariable String role,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByRole(role, pageable));
    }

    /**
     * Get the current user's notifications.
     *
     * @return List of notification response DTOs
     */
    @GetMapping("/me/notifications")
    @Operation(
            summary = "Get current user notifications",
            description = "Retrieves all notifications for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<List<NotificationResponse>> getCurrentUserNotifications() {
        return ResponseEntity.ok(userService.getCurrentUserNotifications());
    }

    /**
     * Get the current user's unread notifications.
     *
     * @return List of unread notification response DTOs
     */
    @GetMapping("/me/notifications/unread")
    @Operation(
            summary = "Get current user unread notifications",
            description = "Retrieves unread notifications for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<List<NotificationResponse>> getCurrentUserUnreadNotifications() {
        return ResponseEntity.ok(userService.getCurrentUserUnreadNotifications());
    }

    /**
     * Mark a notification as read.
     *
     * @param notificationId The ID of the notification to mark as read
     * @return The updated notification response DTO
     */
    @PutMapping("/me/notifications/{notificationId}/read")
    @Operation(
            summary = "Mark notification as read",
            description = "Marks a notification as read for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notification marked as read",
                            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Notification not found")
            }
    )
    public ResponseEntity<NotificationResponse> markNotificationAsRead(
            @Parameter(description = "ID of the notification to mark as read") @PathVariable String notificationId) {
        return ResponseEntity.ok(userService.markNotificationAsRead(notificationId));
    }

    /**
     * Mark all notifications as read for the current user.
     *
     * @return No content response
     */
    @PutMapping("/me/notifications/read-all")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications as read for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "All notifications marked as read"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        userService.markAllNotificationsAsRead();
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a notification.
     *
     * @param notificationId The ID of the notification to delete
     * @return No content response
     */
    @DeleteMapping("/me/notifications/{notificationId}")
    @Operation(
            summary = "Delete notification",
            description = "Deletes a notification for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Notification not found")
            }
    )
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "ID of the notification to delete") @PathVariable String notificationId) {
        userService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a notification for a user (admin only).
     *
     * @param userId The ID of the user to notify
     * @param title The notification title
     * @param message The notification message
     * @param type The notification type
     * @param relatedEntityId The ID of the related entity (optional)
     * @return The created notification response DTO
     */
    @PostMapping("/{userId}/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create notification for user",
            description = "Creates a notification for a specific user. Only accessible by admins.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notification created successfully",
                            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<NotificationResponse> createNotification(
            @Parameter(description = "ID of the user to notify") @PathVariable String userId,
            @Parameter(description = "Notification title") @RequestParam String title,
            @Parameter(description = "Notification message") @RequestParam String message,
            @Parameter(description = "Notification type") @RequestParam String type,
            @Parameter(description = "ID of the related entity (optional)") @RequestParam(required = false) String relatedEntityId) {
        return ResponseEntity.ok(userService.createNotification(userId, title, message, type, relatedEntityId));
    }
}