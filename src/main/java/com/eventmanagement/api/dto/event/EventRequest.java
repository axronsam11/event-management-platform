package com.eventmanagement.api.dto.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for creating or updating events.
 * Contains all fields needed for event operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    private String category;

    private String imageUrl;

    private Map<String, String> additionalInfo;

    @Valid
    private List<SpeakerDto> speakers = new ArrayList<>();

    @Valid
    private List<AgendaItemDto> agenda = new ArrayList<>();

    @Valid
    private List<TicketTypeDto> ticketTypes = new ArrayList<>();

    /**
     * DTO for speaker information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpeakerDto {
        private String id;
        
        @NotBlank(message = "Speaker name is required")
        private String name;
        
        private String bio;
        private String photoUrl;
        private String company;
        private String jobTitle;
        private List<String> socialLinks;
    }

    /**
     * DTO for agenda item information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgendaItemDto {
        private String id;
        
        @NotBlank(message = "Agenda item title is required")
        private String title;
        
        private String description;
        
        @NotNull(message = "Start time is required")
        private LocalDateTime startTime;
        
        @NotNull(message = "End time is required")
        private LocalDateTime endTime;
        
        private String type;
        
        @Valid
        private List<SessionDto> sessions = new ArrayList<>();
    }

    /**
     * DTO for session information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDto {
        private String id;
        
        @NotBlank(message = "Session title is required")
        private String title;
        
        private String description;
        private String location;
        
        @NotNull(message = "Session start time is required")
        private LocalDateTime startTime;
        
        @NotNull(message = "Session end time is required")
        private LocalDateTime endTime;
        
        private List<String> speakerIds;
        private int capacity;
        private String sessionType;
        private Map<String, String> additionalInfo;
    }

    /**
     * DTO for ticket type information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketTypeDto {
        private String id;
        
        @NotBlank(message = "Ticket name is required")
        private String name;
        
        private String description;
        
        @NotNull(message = "Price is required")
        private double price;
        
        @NotNull(message = "Quantity is required")
        private int quantity;
        
        private LocalDateTime saleStartDate;
        private LocalDateTime saleEndDate;
        private boolean isAvailable;
    }
}