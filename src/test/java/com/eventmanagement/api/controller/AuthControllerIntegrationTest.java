package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.auth.AuthRequest;
import com.eventmanagement.api.dto.auth.RegisterRequest;
import com.eventmanagement.api.model.User;
import com.eventmanagement.api.repository.UserRepository;
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

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AuthController.
 * Uses Testcontainers to spin up a MongoDB instance for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerIntegrationTest {

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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_Success() throws Exception {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .roles(Collections.singletonList("ATTENDEE"))
                .build();

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.roles[0]", is("ATTENDEE")));
    }

    @Test
    void registerUser_DuplicateEmail_Failure() throws Exception {
        // Given
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setPhoneNumber("+1234567890");
        existingUser.setRoles(Collections.singletonList("ATTENDEE"));
        existingUser.setEnabled(true);
        userRepository.save(existingUser);

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test@example.com") // Same email as existing user
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .roles(Collections.singletonList("ATTENDEE"))
                .build();

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void loginUser_Success() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("+1234567890");
        user.setRoles(Collections.singletonList("ATTENDEE"));
        user.setEnabled(true);
        userRepository.save(user);

        AuthRequest authRequest = AuthRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.roles[0]", is("ATTENDEE")));
    }

    @Test
    void loginUser_InvalidCredentials_Failure() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("+1234567890");
        user.setRoles(Collections.singletonList("ATTENDEE"));
        user.setEnabled(true);
        userRepository.save(user);

        AuthRequest authRequest = AuthRequest.builder()
                .email("test@example.com")
                .password("wrongpassword") // Wrong password
                .build();

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isUnauthorized());
    }
}