package com.eventmanagement.api.service;

import com.eventmanagement.api.dto.event.EventRequest;
import com.eventmanagement.api.dto.event.EventResponse;
import com.eventmanagement.api.dto.event.RegistrationRequest;
import com.eventmanagement.api.exception.ResourceNotFoundException;
import com.eventmanagement.api.model.Event;
import com.eventmanagement.api.model.User;
import com.eventmanagement.api.repository.EventRepository;
import com.eventmanagement.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling event operations.
 * Manages event creation, updates, retrieval, and registrations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Create a new event.
     *
     * @param eventRequest The event request DTO
     * @return The created event as a response DTO
     */
    public EventResponse createEvent(EventRequest eventRequest) {
        User currentUser = getCurrentUser();
        
        // Check if user has organizer role
        if (!currentUser.getRoles().contains("ORGANIZER") && !currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("Only organizers can create events");
        }

        Event event = mapToEntity(eventRequest);
        event.setOrganizerId(currentUser.getId());
        event.setOrganizerName(currentUser.getFirstName() + " " + currentUser.getLastName());
        event.setStatus("DRAFT");

        // Generate IDs for embedded documents
        generateIds(event);

        Event savedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(savedEvent);
    }

    /**
     * Update an existing event.
     *
     * @param eventId The ID of the event to update
     * @param eventRequest The event request DTO with updated data
     * @return The updated event as a response DTO
     */
    public EventResponse updateEvent(String eventId, EventRequest eventRequest) {
        Event existingEvent = getEventById(eventId);
        User currentUser = getCurrentUser();

        // Check if user is the organizer or an admin
        if (!existingEvent.getOrganizerId().equals(currentUser.getId()) && 
                !currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("You can only update your own events");
        }

        // Update event fields
        existingEvent.setTitle(eventRequest.getTitle());
        existingEvent.setDescription(eventRequest.getDescription());
        existingEvent.setLocation(eventRequest.getLocation());
        existingEvent.setStartDate(eventRequest.getStartDate());
        existingEvent.setEndDate(eventRequest.getEndDate());
        existingEvent.setCategory(eventRequest.getCategory());
        existingEvent.setImageUrl(eventRequest.getImageUrl());
        existingEvent.setAdditionalInfo(eventRequest.getAdditionalInfo());

        // Update speakers
        if (eventRequest.getSpeakers() != null) {
            existingEvent.setSpeakers(eventRequest.getSpeakers().stream()
                    .map(speakerDto -> {
                        Event.Speaker speaker = new Event.Speaker();
                        speaker.setId(speakerDto.getId() != null ? speakerDto.getId() : UUID.randomUUID().toString());
                        speaker.setName(speakerDto.getName());
                        speaker.setBio(speakerDto.getBio());
                        speaker.setPhotoUrl(speakerDto.getPhotoUrl());
                        speaker.setCompany(speakerDto.getCompany());
                        speaker.setJobTitle(speakerDto.getJobTitle());
                        speaker.setSocialLinks(speakerDto.getSocialLinks());
                        return speaker;
                    })
                    .collect(Collectors.toList()));
        }

        // Update agenda items and sessions
        if (eventRequest.getAgenda() != null) {
            existingEvent.setAgenda(eventRequest.getAgenda().stream()
                    .map(agendaItemDto -> {
                        Event.AgendaItem agendaItem = new Event.AgendaItem();
                        agendaItem.setId(agendaItemDto.getId() != null ? agendaItemDto.getId() : UUID.randomUUID().toString());
                        agendaItem.setTitle(agendaItemDto.getTitle());
                        agendaItem.setDescription(agendaItemDto.getDescription());
                        agendaItem.setStartTime(agendaItemDto.getStartTime());
                        agendaItem.setEndTime(agendaItemDto.getEndTime());
                        agendaItem.setType(agendaItemDto.getType());

                        if (agendaItemDto.getSessions() != null) {
                            agendaItem.setSessions(agendaItemDto.getSessions().stream()
                                    .map(sessionDto -> {
                                        Event.Session session = new Event.Session();
                                        session.setId(sessionDto.getId() != null ? sessionDto.getId() : UUID.randomUUID().toString());
                                        session.setTitle(sessionDto.getTitle());
                                        session.setDescription(sessionDto.getDescription());
                                        session.setLocation(sessionDto.getLocation());
                                        session.setStartTime(sessionDto.getStartTime());
                                        session.setEndTime(sessionDto.getEndTime());
                                        session.setSpeakerIds(sessionDto.getSpeakerIds());
                                        session.setCapacity(sessionDto.getCapacity());
                                        session.setSessionType(sessionDto.getSessionType());
                                        session.setAdditionalInfo(sessionDto.getAdditionalInfo());
                                        return session;
                                    })
                                    .collect(Collectors.toList()));
                        }

                        return agendaItem;
                    })
                    .collect(Collectors.toList()));
        }

        // Update ticket types
        if (eventRequest.getTicketTypes() != null) {
            existingEvent.setTicketTypes(eventRequest.getTicketTypes().stream()
                    .map(ticketTypeDto -> {
                        Event.TicketType ticketType = new Event.TicketType();
                        ticketType.setId(ticketTypeDto.getId() != null ? ticketTypeDto.getId() : UUID.randomUUID().toString());
                        ticketType.setName(ticketTypeDto.getName());
                        ticketType.setDescription(ticketTypeDto.getDescription());
                        ticketType.setPrice(ticketTypeDto.getPrice());
                        ticketType.setQuantity(ticketTypeDto.getQuantity());
                        ticketType.setSaleStartDate(ticketTypeDto.getSaleStartDate());
                        ticketType.setSaleEndDate(ticketTypeDto.getSaleEndDate());
                        ticketType.setAvailable(ticketTypeDto.isAvailable());
                        // Preserve sold count if ticket type already exists
                        existingEvent.getTicketTypes().stream()
                                .filter(t -> t.getId().equals(ticketType.getId()))
                                .findFirst()
                                .ifPresent(t -> ticketType.setSold(t.getSold()));
                        return ticketType;
                    })
                    .collect(Collectors.toList()));
        }

        Event updatedEvent = eventRepository.save(existingEvent);
        return EventResponse.fromEntity(updatedEvent);
    }

    /**
     * Get an event by ID.
     *
     * @param eventId The ID of the event to retrieve
     * @return The event as a response DTO
     */
    public EventResponse getEventById(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        return EventResponse.fromEntity(event);
    }

    /**
     * Get all events with pagination.
     *
     * @param pageable Pagination information
     * @return Page of events as response DTOs
     */
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(EventResponse::fromEntity);
    }

    /**
     * Get events by organizer ID with pagination.
     *
     * @param organizerId The ID of the organizer
     * @param pageable Pagination information
     * @return Page of events as response DTOs
     */
    public Page<EventResponse> getEventsByOrganizerId(String organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerId(organizerId, pageable)
                .map(EventResponse::fromEntity);
    }

    /**
     * Get events by category with pagination.
     *
     * @param category The event category
     * @param pageable Pagination information
     * @return Page of events as response DTOs
     */
    public Page<EventResponse> getEventsByCategory(String category, Pageable pageable) {
        return eventRepository.findPublishedEventsByCategory(category, pageable)
                .map(EventResponse::fromEntity);
    }

    /**
     * Get upcoming events with pagination.
     *
     * @param pageable Pagination information
     * @return Page of upcoming events as response DTOs
     */
    public Page<EventResponse> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable)
                .map(EventResponse::fromEntity);
    }

    /**
     * Search events by title or description with pagination.
     *
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching events as response DTOs
     */
    public Page<EventResponse> searchEvents(String searchTerm, Pageable pageable) {
        return eventRepository.searchEvents(searchTerm, pageable)
                .map(EventResponse::fromEntity);
    }

    /**
     * Delete an event by ID.
     *
     * @param eventId The ID of the event to delete
     */
    public void deleteEvent(String eventId) {
        Event event = getEventEntityById(eventId);
        User currentUser = getCurrentUser();

        // Check if user is the organizer or an admin
        if (!event.getOrganizerId().equals(currentUser.getId()) && 
                !currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("You can only delete your own events");
        }

        eventRepository.delete(event);
    }

    /**
     * Publish an event.
     *
     * @param eventId The ID of the event to publish
     * @return The published event as a response DTO
     */
    public EventResponse publishEvent(String eventId) {
        Event event = getEventEntityById(eventId);
        User currentUser = getCurrentUser();

        // Check if user is the organizer or an admin
        if (!event.getOrganizerId().equals(currentUser.getId()) && 
                !currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("You can only publish your own events");
        }

        event.setStatus("PUBLISHED");
        Event publishedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(publishedEvent);
    }

    /**
     * Cancel an event.
     *
     * @param eventId The ID of the event to cancel
     * @return The cancelled event as a response DTO
     */
    public EventResponse cancelEvent(String eventId) {
        Event event = getEventEntityById(eventId);
        User currentUser = getCurrentUser();

        // Check if user is the organizer or an admin
        if (!event.getOrganizerId().equals(currentUser.getId()) && 
                !currentUser.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("You can only cancel your own events");
        }

        event.setStatus("CANCELLED");
        Event cancelledEvent = eventRepository.save(event);
        return EventResponse.fromEntity(cancelledEvent);
    }

    /**
     * Register for an event.
     *
     * @param registrationRequest The registration request DTO
     * @return The updated event as a response DTO
     */
    public EventResponse registerForEvent(RegistrationRequest registrationRequest) {
        Event event = getEventEntityById(registrationRequest.getEventId());
        User currentUser = getCurrentUser();

        // Check if event is published
        if (!"PUBLISHED".equals(event.getStatus())) {
            throw new IllegalStateException("Cannot register for an event that is not published");
        }

        // Find the ticket type
        Event.TicketType ticketType = event.getTicketTypes().stream()
                .filter(t -> t.getId().equals(registrationRequest.getTicketTypeId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type", "id", registrationRequest.getTicketTypeId()));

        // Check if tickets are available
        if (!ticketType.isAvailable() || ticketType.getSold() >= ticketType.getQuantity()) {
            throw new IllegalStateException("No tickets available for this ticket type");
        }

        // Check if user is already registered
        boolean alreadyRegistered = event.getRegistrations().stream()
                .anyMatch(r -> r.getUserId().equals(currentUser.getId()));

        if (alreadyRegistered) {
            throw new IllegalStateException("You are already registered for this event");
        }

        // Create registration
        Event.Registration registration = Event.Registration.builder()
                .id(UUID.randomUUID().toString())
                .userId(currentUser.getId())
                .userName(currentUser.getFirstName() + " " + currentUser.getLastName())
                .userEmail(currentUser.getEmail())
                .ticketTypeId(ticketType.getId())
                .ticketTypeName(ticketType.getName())
                .amountPaid(registrationRequest.getAmountPaid())
                .status("CONFIRMED")
                .registrationDate(LocalDateTime.now())
                .confirmationCode(generateConfirmationCode())
                .sessionIds(registrationRequest.getSessionIds())
                .attendeeInfo(registrationRequest.getAttendeeInfo())
                .build();

        // Add registration to event
        event.getRegistrations().add(registration);

        // Update ticket sold count
        ticketType.setSold(ticketType.getSold() + 1);

        // Save event
        Event updatedEvent = eventRepository.save(event);

        // Create notification for user
        createRegistrationNotification(currentUser, event);

        return EventResponse.fromEntity(updatedEvent);
    }

    /**
     * Get events that a user is registered for.
     *
     * @param userId The ID of the user
     * @return List of events the user is registered for as response DTOs
     */
    public List<EventResponse> getEventsForAttendee(String userId) {
        return eventRepository.findEventsByAttendeeId(userId).stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get an event entity by ID.
     *
     * @param eventId The ID of the event to retrieve
     * @return The event entity
     */
    private Event getEventEntityById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
    }

    /**
     * Get the current authenticated user.
     *
     * @return The current user entity
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    /**
     * Map an event request DTO to an event entity.
     *
     * @param eventRequest The event request DTO
     * @return The event entity
     */
    private Event mapToEntity(EventRequest eventRequest) {
        Event event = new Event();
        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setLocation(eventRequest.getLocation());
        event.setStartDate(eventRequest.getStartDate());
        event.setEndDate(eventRequest.getEndDate());
        event.setCategory(eventRequest.getCategory());
        event.setImageUrl(eventRequest.getImageUrl());
        event.setAdditionalInfo(eventRequest.getAdditionalInfo());

        // Map speakers
        if (eventRequest.getSpeakers() != null) {
            event.setSpeakers(eventRequest.getSpeakers().stream()
                    .map(speakerDto -> {
                        Event.Speaker speaker = new Event.Speaker();
                        speaker.setName(speakerDto.getName());
                        speaker.setBio(speakerDto.getBio());
                        speaker.setPhotoUrl(speakerDto.getPhotoUrl());
                        speaker.setCompany(speakerDto.getCompany());
                        speaker.setJobTitle(speakerDto.getJobTitle());
                        speaker.setSocialLinks(speakerDto.getSocialLinks());
                        return speaker;
                    })
                    .collect(Collectors.toList()));
        }

        // Map agenda items and sessions
        if (eventRequest.getAgenda() != null) {
            event.setAgenda(eventRequest.getAgenda().stream()
                    .map(agendaItemDto -> {
                        Event.AgendaItem agendaItem = new Event.AgendaItem();
                        agendaItem.setTitle(agendaItemDto.getTitle());
                        agendaItem.setDescription(agendaItemDto.getDescription());
                        agendaItem.setStartTime(agendaItemDto.getStartTime());
                        agendaItem.setEndTime(agendaItemDto.getEndTime());
                        agendaItem.setType(agendaItemDto.getType());

                        if (agendaItemDto.getSessions() != null) {
                            agendaItem.setSessions(agendaItemDto.getSessions().stream()
                                    .map(sessionDto -> {
                                        Event.Session session = new Event.Session();
                                        session.setTitle(sessionDto.getTitle());
                                        session.setDescription(sessionDto.getDescription());
                                        session.setLocation(sessionDto.getLocation());
                                        session.setStartTime(sessionDto.getStartTime());
                                        session.setEndTime(sessionDto.getEndTime());
                                        session.setSpeakerIds(sessionDto.getSpeakerIds());
                                        session.setCapacity(sessionDto.getCapacity());
                                        session.setSessionType(sessionDto.getSessionType());
                                        session.setAdditionalInfo(sessionDto.getAdditionalInfo());
                                        return session;
                                    })
                                    .collect(Collectors.toList()));
                        }

                        return agendaItem;
                    })
                    .collect(Collectors.toList()));
        }

        // Map ticket types
        if (eventRequest.getTicketTypes() != null) {
            event.setTicketTypes(eventRequest.getTicketTypes().stream()
                    .map(ticketTypeDto -> {
                        Event.TicketType ticketType = new Event.TicketType();
                        ticketType.setName(ticketTypeDto.getName());
                        ticketType.setDescription(ticketTypeDto.getDescription());
                        ticketType.setPrice(ticketTypeDto.getPrice());
                        ticketType.setQuantity(ticketTypeDto.getQuantity());
                        ticketType.setSold(0);
                        ticketType.setSaleStartDate(ticketTypeDto.getSaleStartDate());
                        ticketType.setSaleEndDate(ticketTypeDto.getSaleEndDate());
                        ticketType.setAvailable(ticketTypeDto.isAvailable());
                        return ticketType;
                    })
                    .collect(Collectors.toList()));
        }

        return event;
    }

    /**
     * Generate IDs for embedded documents in an event.
     *
     * @param event The event entity
     */
    private void generateIds(Event event) {
        // Generate IDs for speakers
        if (event.getSpeakers() != null) {
            event.getSpeakers().forEach(speaker -> {
                if (speaker.getId() == null) {
                    speaker.setId(UUID.randomUUID().toString());
                }
            });
        }

        // Generate IDs for agenda items and sessions
        if (event.getAgenda() != null) {
            event.getAgenda().forEach(agendaItem -> {
                if (agendaItem.getId() == null) {
                    agendaItem.setId(UUID.randomUUID().toString());
                }
                if (agendaItem.getSessions() != null) {
                    agendaItem.getSessions().forEach(session -> {
                        if (session.getId() == null) {
                            session.setId(UUID.randomUUID().toString());
                        }
                    });
                }
            });
        }

        // Generate IDs for ticket types
        if (event.getTicketTypes() != null) {
            event.getTicketTypes().forEach(ticketType -> {
                if (ticketType.getId() == null) {
                    ticketType.setId(UUID.randomUUID().toString());
                }
            });
        }
    }

    /**
     * Generate a confirmation code for event registration.
     *
     * @return The generated confirmation code
     */
    private String generateConfirmationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Create a notification for event registration.
     *
     * @param user The user to notify
     * @param event The event the user registered for
     */
    private void createRegistrationNotification(User user, Event event) {
        User.Notification notification = User.Notification.builder()
                .id(UUID.randomUUID().toString())
                .title("Registration Confirmation")
                .message("You have successfully registered for " + event.getTitle())
                .read(false)
                .createdAt(LocalDateTime.now())
                .type("REGISTRATION_CONFIRMATION")
                .relatedEntityId(event.getId())
                .build();

        user.getNotifications().add(notification);
        userRepository.save(user);
    }
}