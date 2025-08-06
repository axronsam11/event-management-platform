package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.auth.LoginRequest;
import com.eventmanagement.api.dto.auth.RegisterRequest;
import com.eventmanagement.api.dto.user.NotificationResponse;
import com.eventmanagement.api.dto.user.UserProfileRequest;
import com.eventmanagement.api.dto.user.UserProfileResponse;
import com.eventmanagement.api.model.Role;
import com.eventmanagement.api.model.User;
import com.eventmanagement.api.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.6"));

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

    private String adminToken;
    private String attendeeToken;
    private String organizerToken;
    private User adminUser;
    private User attendeeUser;
    private User organizerUser;

    @BeforeEach
    void setUp() throws Exception {
        // Register and login users with different roles
        adminUser = registerAndLoginUser("admin@example.com", "Admin", "User", "password", Collections.singleton(Role.ADMIN));
        adminToken = getAuthToken("admin@example.com", "password");

        attendeeUser = registerAndLoginUser("attendee@example.com", "Attendee", "User", "password", Collections.singleton(Role.ATTENDEE));
        attendeeToken = getAuthToken("attendee@example.com", "password");

        organizerUser = registerAndLoginUser("organizer@example.com", "Organizer", "User", "password", Collections.singleton(Role.ORGANIZER));
        organizerToken = getAuthToken("organizer@example.com", "password");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("attendee@example.com"))
                .andExpect(jsonPath("$.firstName").value("Attendee"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void updateCurrentUser_Success() throws Exception {
        UserProfileRequest request = new UserProfileRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setPhoneNumber("+1234567890");

        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + attendeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));
    }

    @Test
    void getUserById_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/users/" + attendeeUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("attendee@example.com"));
    }

    @Test
    void getUserById_NonAdmin_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserById_Admin_Success() throws Exception {
        UserProfileRequest request = new UserProfileRequest();
        request.setFirstName("Admin");
        request.setLastName("Updated");
        request.setPhoneNumber("+9876543210");
        request.setRoles(Collections.singleton(Role.ADMIN));
        request.setEnabled(true);

        mockMvc.perform(put("/api/users/" + attendeeUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));

        // Verify role was updated
        User updatedUser = userRepository.findById(attendeeUser.getId()).orElseThrow();
        assertTrue(updatedUser.getRoles().contains(Role.ADMIN));
    }

    @Test
    void deleteUser_Admin_Success() throws Exception {
        mockMvc.perform(delete("/api/users/" + attendeeUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.findById(attendeeUser.getId()).isPresent());
    }

    @Test
    void getAllUsers_Admin_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Map<String, Object> pageResponse = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        
        assertEquals(3, pageResponse.get("totalElements"));
    }

    @Test
    void searchUsersByName_Admin_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/search?name=Admin")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Map<String, Object> pageResponse = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        
        assertEquals(1, pageResponse.get("totalElements"));
    }

    @Test
    void getUsersByRole_Admin_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/role/ORGANIZER")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Map<String, Object> pageResponse = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        
        assertEquals(1, pageResponse.get("totalElements"));
    }

    @Test
    void createNotification_Admin_Success() throws Exception {
        Map<String, String> notificationRequest = Map.of(
                "title", "Test Notification",
                "message", "This is a test notification",
                "type", "SYSTEM"
        );

        mockMvc.perform(post("/api/users/" + attendeeUser.getId() + "/notifications")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated());

        // Verify notification was created
        User user = userRepository.findById(attendeeUser.getId()).orElseThrow();
        assertEquals(1, user.getNotifications().size());
        assertEquals("Test Notification", user.getNotifications().get(0).getTitle());
    }

    @Test
    void getUserNotifications_Success() throws Exception {
        // Create a notification for the attendee
        createNotificationForUser(attendeeUser.getId(), "Test Notification", "This is a test notification", "SYSTEM");

        MvcResult result = mockMvc.perform(get("/api/users/me/notifications")
                .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isOk())
                .andReturn();

        List<NotificationResponse> notifications = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<NotificationResponse>>() {});

        assertEquals(1, notifications.size());
        assertEquals("Test Notification", notifications.get(0).getTitle());
    }

    @Test
    void markNotificationAsRead_Success() throws Exception {
        // Create a notification for the attendee
        String notificationId = createNotificationForUser(
                attendeeUser.getId(), "Test Notification", "This is a test notification", "SYSTEM");

        mockMvc.perform(put("/api/users/me/notifications/" + notificationId + "/read")
                .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isOk());

        // Verify notification was marked as read
        User user = userRepository.findById(attendeeUser.getId()).orElseThrow();
        assertTrue(user.getNotifications().get(0).isRead());
    }

    @Test
    void markAllNotificationsAsRead_Success() throws Exception {
        // Create multiple notifications for the attendee
        createNotificationForUser(attendeeUser.getId(), "Notification 1", "Content 1", "SYSTEM");
        createNotificationForUser(attendeeUser.getId(), "Notification 2", "Content 2", "SYSTEM");

        mockMvc.perform(put("/api/users/me/notifications/read-all")
                .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isOk());

        // Verify all notifications were marked as read
        User user = userRepository.findById(attendeeUser.getId()).orElseThrow();
        assertTrue(user.getNotifications().stream().allMatch(n -> n.isRead()));
    }

    @Test
    void deleteNotification_Success() throws Exception {
        // Create a notification for the attendee
        String notificationId = createNotificationForUser(
                attendeeUser.getId(), "Test Notification", "This is a test notification", "SYSTEM");

        mockMvc.perform(delete("/api/users/me/notifications/" + notificationId)
                .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isNoContent());

        // Verify notification was deleted
        User user = userRepository.findById(attendeeUser.getId()).orElseThrow();
        assertTrue(user.getNotifications().isEmpty());
    }

    // Helper methods

    private User registerAndLoginUser(String email, String firstName, String lastName, String password, 
                                     java.util.Set<Role> roles) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setFirstName(firstName);
        registerRequest.setLastName(lastName);
        registerRequest.setPassword(password);
        registerRequest.setRoles(roles);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        return userRepository.findByEmail(email).orElseThrow();
    }

    private String getAuthToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<Map<String, String>>() {});

        return response.get("token");
    }

    private String createNotificationForUser(String userId, String title, String message, String type) {
        User user = userRepository.findById(userId).orElseThrow();
        
        com.eventmanagement.api.model.Notification notification = new com.eventmanagement.api.model.Notification();
        notification.setId(java.util.UUID.randomUUID().toString());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setType(type);
        
        if (user.getNotifications() == null) {
            user.setNotifications(new java.util.ArrayList<>());
        }
        user.getNotifications().add(notification);
        userRepository.save(user);
        
        return notification.getId();
    }
}