package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.event.EventRequest;
import com.eventmanagement.api.dto.event.EventResponse;
import com.eventmanagement.api.dto.event.RegistrationRequest;
import com.eventmanagement.api.service.EventService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for event operations.
 * Provides endpoints for creating, updating, retrieving, and managing events.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for event operations")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService eventService;

    /**
     * Create a new event.
     *
     * @param eventRequest The event request DTO
     * @return The created event as a response DTO
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(
            summary = "Create a new event",
            description = "Creates a new event with the provided details. Only organizers and admins can create events.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Event created successfully",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        return new ResponseEntity<>(eventService.createEvent(eventRequest), HttpStatus.CREATED);
    }

    /**
     * Update an existing event.
     *
     * @param eventId The ID of the event to update
     * @param eventRequest The event request DTO with updated data
     * @return The updated event as a response DTO
     */
    @PutMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(
            summary = "Update an event",
            description = "Updates an existing event with the provided details. Only the event organizer or admins can update events.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event updated successfully",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "ID of the event to update") @PathVariable String eventId,
            @Valid @RequestBody EventRequest eventRequest) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, eventRequest));
    }

    /**
     * Get an event by ID.
     *
     * @param eventId The ID of the event to retrieve
     * @return The event as a response DTO
     */
    @GetMapping("/{eventId}")
    @Operation(
            summary = "Get an event by ID",
            description = "Retrieves an event by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event retrieved successfully",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ResponseEntity<EventResponse> getEventById(
            @Parameter(description = "ID of the event to retrieve") @PathVariable String eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    /**
     * Get all events with pagination.
     *
     * @param pageable Pagination information
     * @return Page of events as response DTOs
     */
    @GetMapping
    @Operation(
            summary = "Get all events",
            description = "Retrieves all events with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
            }
    )
    public ResponseEntity<Page<EventResponse>> getAllEvents(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(eventService.getAllEvents(pageable));
    }

    /**
     * Get events by organizer ID with pagination.
     *
     * @param organizerId The ID of the organizer
     * @param pageable Pagination information
     * @return Page of events as response DTOs
     */
    @GetMapping("/organizer/{organizerId}")
    @Operation(
            summary = "Get events by organizer ID",
            description = "Retrieves events created by a specific organizer with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
            }
    )
    public ResponseEntity<Page<EventResponse>> getEventsByOrganizerId(
            @Parameter(description = "ID of the organizer") @PathVariable String organizerId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(eventService.getEventsByOrganizerId(organizerId, pageable));
    }

    /**
     * Get events by category with pagination.
     *
     * @param category The event category
     * @param pageable Pagination information
     * @return Page of events as response DTOs
     */
    @GetMapping("/category/{category}")
    @Operation(
            summary = "Get events by category",
            description = "Retrieves published events by category with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
            }
    )
    public ResponseEntity<Page<EventResponse>> getEventsByCategory(
            @Parameter(description = "Event category") @PathVariable String category,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(eventService.getEventsByCategory(category, pageable));
    }

    /**
     * Get upcoming events with pagination.
     *
     * @param pageable Pagination information
     * @return Page of upcoming events as response DTOs
     */
    @GetMapping("/upcoming")
    @Operation(
            summary = "Get upcoming events",
            description = "Retrieves upcoming events with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
            }
    )
    public ResponseEntity<Page<EventResponse>> getUpcomingEvents(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(pageable));
    }

    /**
     * Search events by title or description with pagination.
     *
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching events as response DTOs
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search events",
            description = "Searches events by title or description with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
            }
    )
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(eventService.searchEvents(searchTerm, pageable));
    }

    /**
     * Delete an event by ID.
     *
     * @param eventId The ID of the event to delete
     * @return No content response
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(
            summary = "Delete an event",
            description = "Deletes an event by its ID. Only the event organizer or admins can delete events.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID of the event to delete") @PathVariable String eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish an event.
     *
     * @param eventId The ID of the event to publish
     * @return The published event as a response DTO
     */
    @PutMapping("/{eventId}/publish")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(
            summary = "Publish an event",
            description = "Publishes an event by its ID. Only the event organizer or admins can publish events.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event published successfully",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ResponseEntity<EventResponse> publishEvent(
            @Parameter(description = "ID of the event to publish") @PathVariable String eventId) {
        return ResponseEntity.ok(eventService.publishEvent(eventId));
    }

    /**
     * Cancel an event.
     *
     * @param eventId The ID of the event to cancel
     * @return The cancelled event as a response DTO
     */
    @PutMapping("/{eventId}/cancel")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(
            summary = "Cancel an event",
            description = "Cancels an event by its ID. Only the event organizer or admins can cancel events.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event cancelled successfully",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ResponseEntity<EventResponse> cancelEvent(
            @Parameter(description = "ID of the event to cancel") @PathVariable String eventId) {
        return ResponseEntity.ok(eventService.cancelEvent(eventId));
    }

    /**
     * Register for an event.
     *
     * @param registrationRequest The registration request DTO
     * @return The updated event as a response DTO
     */
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ORGANIZER', 'ADMIN')")
    @Operation(
            summary = "Register for an event",
            description = "Registers the current user for an event.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration successful",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or registration not possible"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Event or ticket type not found")
            }
    )
    public ResponseEntity<EventResponse> registerForEvent(@Valid @RequestBody RegistrationRequest registrationRequest) {
        return ResponseEntity.ok(eventService.registerForEvent(registrationRequest));
    }

    /**
     * Get events that a user is registered for.
     *
     * @param userId The ID of the user
     * @return List of events the user is registered for as response DTOs
     */
    @GetMapping("/attendee/{userId}")
    @Operation(
            summary = "Get events for attendee",
            description = "Retrieves events that a user is registered for.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
            }
    )
    public ResponseEntity<List<EventResponse>> getEventsForAttendee(
            @Parameter(description = "ID of the user") @PathVariable String userId) {
        return ResponseEntity.ok(eventService.getEventsForAttendee(userId));
    }
}