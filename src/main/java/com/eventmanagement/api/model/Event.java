package com.eventmanagement.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Event document model for MongoDB.
 * Uses embedded documents for agenda, sessions, speakers, and registrations
 * to demonstrate MongoDB's document-oriented capabilities.
 */
@Document(collection = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "organizer_status_idx", def = "{organizerId: 1, status: 1}")
public class Event {

    @Id
    private String id;
    
    private String title;
    
    private String description;
    
    private String location;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Indexed
    private String organizerId;
    
    private String organizerName;
    
    private String status; // DRAFT, PUBLISHED, CANCELLED, COMPLETED
    
    private String category;
    
    private String imageUrl;
    
    private Map<String, String> additionalInfo; // Flexible schema for extra event details
    
    @Builder.Default
    private List<Speaker> speakers = new ArrayList<>();
    
    @Builder.Default
    private List<AgendaItem> agenda = new ArrayList<>();
    
    @Builder.Default
    private List<TicketType> ticketTypes = new ArrayList<>();
    
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Embedded speaker document.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Speaker {
        private String id;
        private String name;
        private String bio;
        private String photoUrl;
        private String company;
        private String jobTitle;
        private List<String> socialLinks;
    }
    
    /**
     * Embedded agenda item document with nested sessions.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgendaItem {
        private String id;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String type; // e.g., "BREAK", "SESSION", "KEYNOTE"
        private List<Session> sessions;
    }
    
    /**
     * Embedded session document for detailed agenda items.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Session {
        private String id;
        private String title;
        private String description;
        private String location; // e.g., "Room A", "Main Hall"
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<String> speakerIds;
        private int capacity;
        private String sessionType; // e.g., "WORKSHOP", "PRESENTATION", "PANEL"
        private Map<String, String> additionalInfo;
    }
    
    /**
     * Embedded ticket type document.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketType {
        private String id;
        private String name;
        private String description;
        private double price;
        private int quantity;
        private int sold;
        private LocalDateTime saleStartDate;
        private LocalDateTime saleEndDate;
        private boolean isAvailable;
    }
    
    /**
     * Embedded registration document for attendee registrations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Registration {
        private String id;
        private String userId;
        private String userName;
        private String userEmail;
        private String ticketTypeId;
        private String ticketTypeName;
        private double amountPaid;
        private String status; // CONFIRMED, CANCELLED, PENDING
        private LocalDateTime registrationDate;
        private String confirmationCode;
        private List<String> sessionIds; // Optional: for tracking session attendance
        private Map<String, String> attendeeInfo; // Custom fields for registration
    }
}