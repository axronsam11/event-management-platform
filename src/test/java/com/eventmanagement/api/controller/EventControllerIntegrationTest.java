package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.event.EventRequest;
import com.eventmanagement.api.model.Event;
import com.eventmanagement.api.model.User;
import com.eventmanagement.api.repository.EventRepository;
import com.eventmanagement.api.repository.UserRepository;
import com.eventmanagement.api.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the EventController.
 * Uses Testcontainers to spin up a MongoDB instance for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class EventControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.9");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User organizerUser;
    private User attendeeUser;
    private User adminUser;
    private String organizerToken;
    private String attendeeToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create organizer user
        organizerUser = new User();
        organizerUser.setEmail("organizer@example.com");
        organizerUser.setPassword(passwordEncoder.encode("password123"));
        organizerUser.setFirstName("Organizer");
        organizerUser.setLastName("User");
        organizerUser.setPhoneNumber("+1234567890");
        organizerUser.setRoles(Collections.singletonList("ORGANIZER"));
        organizerUser.setEnabled(true);
        organizerUser = userRepository.save(organizerUser);
        organizerToken = jwtTokenProvider.generateToken(organizerUser.getEmail(), organizerUser.getRoles());

        // Create attendee user
        attendeeUser = new User();
        attendeeUser.setEmail("attendee@example.com");
        attendeeUser.setPassword(passwordEncoder.encode("password123"));
        attendeeUser.setFirstName("Attendee");
        attendeeUser.setLastName("User");
        attendeeUser.setPhoneNumber("+1234567890");
        attendeeUser.setRoles(Collections.singletonList("ATTENDEE"));
        attendeeUser.setEnabled(true);
        attendeeUser = userRepository.save(attendeeUser);
        attendeeToken = jwtTokenProvider.generateToken(attendeeUser.getEmail(), attendeeUser.getRoles());

        // Create admin user
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPhoneNumber("+1234567890");
        adminUser.setRoles(Collections.singletonList("ADMIN"));
        adminUser.setEnabled(true);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser.getEmail(), adminUser.getRoles());
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createEvent_AsOrganizer_Success() throws Exception {
        // Given
        EventRequest eventRequest = createSampleEventRequest();

        // When
        ResultActions response = mockMvc.perform(post("/api/events")
                .header("Authorization", "Bearer " + organizerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Test Event")))
                .andExpect(jsonPath("$.description", is("This is a test event")))
                .andExpect(jsonPath("$.organizerId", is(organizerUser.getId())))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }

    @Test
    void createEvent_AsAttendee_Failure() throws Exception {
        // Given
        EventRequest eventRequest = createSampleEventRequest();

        // When
        ResultActions response = mockMvc.perform(post("/api/events")
                .header("Authorization", "Bearer " + attendeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void getEventById_Success() throws Exception {
        // Given
        Event event = createSampleEvent(organizerUser.getId());
        event = eventRepository.save(event);

        // When
        ResultActions response = mockMvc.perform(get("/api/events/{eventId}", event.getId())
                .header("Authorization", "Bearer " + attendeeToken));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(event.getId())))
                .andExpect(jsonPath("$.title", is("Test Event")))
                .andExpect(jsonPath("$.description", is("This is a test event")))
                .andExpect(jsonPath("$.organizerId", is(organizerUser.getId())));
    }

    @Test
    void updateEvent_AsOrganizer_Success() throws Exception {
        // Given
        Event event = createSampleEvent(organizerUser.getId());
        event = eventRepository.save(event);

        EventRequest updateRequest = createSampleEventRequest();
        updateRequest.setTitle("Updated Event Title");
        updateRequest.setDescription("Updated event description");

        // When
        ResultActions response = mockMvc.perform(put("/api/events/{eventId}", event.getId())
                .header("Authorization", "Bearer " + organizerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(event.getId())))
                .andExpect(jsonPath("$.title", is("Updated Event Title")))
                .andExpect(jsonPath("$.description", is("Updated event description")));
    }

    @Test
    void updateEvent_AsOtherOrganizer_Failure() throws Exception {
        // Given
        // Create another organizer
        User otherOrganizer = new User();
        otherOrganizer.setEmail("other.organizer@example.com");
        otherOrganizer.setPassword(passwordEncoder.encode("password123"));
        otherOrganizer.setFirstName("Other");
        otherOrganizer.setLastName("Organizer");
        otherOrganizer.setPhoneNumber("+1234567890");
        otherOrganizer.setRoles(Collections.singletonList("ORGANIZER"));
        otherOrganizer.setEnabled(true);
        otherOrganizer = userRepository.save(otherOrganizer);
        String otherOrganizerToken = jwtTokenProvider.generateToken(otherOrganizer.getEmail(), otherOrganizer.getRoles());

        // Create event owned by the first organizer
        Event event = createSampleEvent(organizerUser.getId());
        event = eventRepository.save(event);

        EventRequest updateRequest = createSampleEventRequest();
        updateRequest.setTitle("Updated Event Title");

        // When - Other organizer tries to update the event
        ResultActions response = mockMvc.perform(put("/api/events/{eventId}", event.getId())
                .header("Authorization", "Bearer " + otherOrganizerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEvent_AsAdmin_Success() throws Exception {
        // Given
        Event event = createSampleEvent(organizerUser.getId());
        event = eventRepository.save(event);

        EventRequest updateRequest = createSampleEventRequest();
        updateRequest.setTitle("Admin Updated Title");

        // When - Admin updates the event
        ResultActions response = mockMvc.perform(put("/api/events/{eventId}", event.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Admin Updated Title")));
    }

    @Test
    void deleteEvent_AsOrganizer_Success() throws Exception {
        // Given
        Event event = createSampleEvent(organizerUser.getId());
        event = eventRepository.save(event);

        // When
        ResultActions response = mockMvc.perform(delete("/api/events/{eventId}", event.getId())
                .header("Authorization", "Bearer " + organizerToken));

        // Then
        response.andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void publishEvent_AsOrganizer_Success() throws Exception {
        // Given
        Event event = createSampleEvent(organizerUser.getId());
        event = eventRepository.save(event);

        // When
        ResultActions response = mockMvc.perform(put("/api/events/{eventId}/publish", event.getId())
                .header("Authorization", "Bearer " + organizerToken));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PUBLISHED")));
    }

    @Test
    void getAllEvents_Success() throws Exception {
        // Given
        Event event1 = createSampleEvent(organizerUser.getId());
        event1.setTitle("Event 1");
        eventRepository.save(event1);

        Event event2 = createSampleEvent(organizerUser.getId());
        event2.setTitle("Event 2");
        eventRepository.save(event2);

        // When
        ResultActions response = mockMvc.perform(get("/api/events")
                .header("Authorization", "Bearer " + attendeeToken));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2)));
    }

    @Test
    void getEventsByCategory_Success() throws Exception {
        // Given
        Event event1 = createSampleEvent(organizerUser.getId());
        event1.setCategory("CONFERENCE");
        event1.setStatus("PUBLISHED");
        eventRepository.save(event1);

        Event event2 = createSampleEvent(organizerUser.getId());
        event2.setCategory("WORKSHOP");
        event2.setStatus("PUBLISHED");
        eventRepository.save(event2);

        // When
        ResultActions response = mockMvc.perform(get("/api/events/category/{category}", "CONFERENCE")
                .header("Authorization", "Bearer " + attendeeToken));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].category", is("CONFERENCE")));
    }

    private EventRequest createSampleEventRequest() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setTitle("Test Event");
        eventRequest.setDescription("This is a test event");
        eventRequest.setLocation("Test Location");
        eventRequest.setStartDate(LocalDateTime.now().plusDays(7));
        eventRequest.setEndDate(LocalDateTime.now().plusDays(8));
        eventRequest.setCategory("CONFERENCE");
        eventRequest.setImageUrl("https://example.com/image.jpg");
        
        // Add a ticket type
        EventRequest.TicketTypeDto ticketType = new EventRequest.TicketTypeDto();
        ticketType.setName("Regular");
        ticketType.setDescription("Regular ticket");
        ticketType.setPrice(100.0);
        ticketType.setQuantity(100);
        ticketType.setSaleStartDate(LocalDateTime.now());
        ticketType.setSaleEndDate(LocalDateTime.now().plusDays(5));
        ticketType.setAvailable(true);
        eventRequest.setTicketTypes(Collections.singletonList(ticketType));
        
        return eventRequest;
    }

    private Event createSampleEvent(String organizerId) {
        Event event = new Event();
        event.setTitle("Test Event");
        event.setDescription("This is a test event");
        event.setLocation("Test Location");
        event.setStartDate(LocalDateTime.now().plusDays(7));
        event.setEndDate(LocalDateTime.now().plusDays(8));
        event.setOrganizerId(organizerId);
        event.setOrganizerName("Organizer User");
        event.setStatus("DRAFT");
        event.setCategory("CONFERENCE");
        event.setImageUrl("https://example.com/image.jpg");
        
        // Add a ticket type
        Event.TicketType ticketType = new Event.TicketType();
        ticketType.setId("ticket-type-1");
        ticketType.setName("Regular");
        ticketType.setDescription("Regular ticket");
        ticketType.setPrice(100.0);
        ticketType.setQuantity(100);
        ticketType.setSold(0);
        ticketType.setSaleStartDate(LocalDateTime.now());
        ticketType.setSaleEndDate(LocalDateTime.now().plusDays(5));
        ticketType.setAvailable(true);
        event.setTicketTypes(Collections.singletonList(ticketType));
        
        return event;
    }
}