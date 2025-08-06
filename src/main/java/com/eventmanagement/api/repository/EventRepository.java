package com.eventmanagement.api.repository;

import com.eventmanagement.api.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for Event document operations.
 * Provides methods for querying and manipulating event data.
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    /**
     * Find events by organizer ID.
     * Used to retrieve all events created by a specific organizer.
     *
     * @param organizerId The ID of the organizer
     * @return List of events organized by the specified user
     */
    List<Event> findByOrganizerId(String organizerId);

    /**
     * Find events by organizer ID with pagination.
     *
     * @param organizerId The ID of the organizer
     * @param pageable Pagination information
     * @return Page of events organized by the specified user
     */
    Page<Event> findByOrganizerId(String organizerId, Pageable pageable);

    /**
     * Find published events by category.
     *
     * @param category The event category
     * @param pageable Pagination information
     * @return Page of published events in the specified category
     */
    @Query("{\"category\": ?0, \"status\": \"PUBLISHED\"}")
    Page<Event> findPublishedEventsByCategory(String category, Pageable pageable);

    /**
     * Find upcoming events (start date is in the future and status is PUBLISHED).
     *
     * @param now The current date and time
     * @param pageable Pagination information
     * @return Page of upcoming events
     */
    @Query("{\"startDate\": {$gt: ?0}, \"status\": \"PUBLISHED\"}")
    Page<Event> findUpcomingEvents(LocalDateTime now, Pageable pageable);

    /**
     * Search events by title or description (case insensitive).
     *
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching events
     */
    @Query("{$or: [{\"title\": {$regex: ?0, $options: 'i'}}, {\"description\": {$regex: ?0, $options: 'i'}}], \"status\": \"PUBLISHED\"}")
    Page<Event> searchEvents(String searchTerm, Pageable pageable);

    /**
     * Find events by location (case insensitive).
     *
     * @param location The location to search for
     * @param pageable Pagination information
     * @return Page of events at the specified location
     */
    @Query("{\"location\": {$regex: ?0, $options: 'i'}, \"status\": \"PUBLISHED\"}")
    Page<Event> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    /**
     * Find events that have a specific user registered.
     * Demonstrates querying embedded documents in MongoDB.
     *
     * @param userId The ID of the registered user
     * @return List of events the user is registered for
     */
    @Query("{\"registrations.userId\": ?0}")
    List<Event> findEventsByAttendeeId(String userId);

    /**
     * Count the number of events by status.
     *
     * @param status The event status
     * @return The count of events with the specified status
     */
    long countByStatus(String status);
}