package com.eventmanagement.api.repository;

import com.eventmanagement.api.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for User document operations.
 * Provides methods for querying and manipulating user data.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find a user by email address.
     * Used for authentication and user lookup.
     *
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists.
     *
     * @param email The email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role.
     * Useful for administrative functions.
     *
     * @param role The role to search for
     * @return List of users with the specified role
     */
    List<User> findByRolesContaining(String role);

    /**
     * Custom query to find users with unread notifications.
     * Demonstrates MongoDB's ability to query embedded documents.
     *
     * @return List of users with unread notifications
     */
    @Query("{\"notifications.read\": false}")
    List<User> findUsersWithUnreadNotifications();

    /**
     * Custom query to find users by partial name match (case insensitive).
     *
     * @param name The name fragment to search for
     * @return List of matching users
     */
    @Query("{$or: [{\"firstName\": {$regex: ?0, $options: 'i'}}, {\"lastName\": {$regex: ?0, $options: 'i'}}]}")
    List<User> findByNameContainingIgnoreCase(String name);
}