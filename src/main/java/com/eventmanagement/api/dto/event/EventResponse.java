package com.eventmanagement.api.dto.event;

import com.eventmanagement.api.model.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for event responses.
 * Contains all event data for client consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private String id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String organizerId;
    private String organizerName;
    private String status;
    private String category;
    private String imageUrl;
    private Map<String, String> additionalInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<SpeakerDto> speakers = new ArrayList<>();
    
    @Builder.Default
    private List<AgendaItemDto> agenda = new ArrayList<>();
    
    @Builder.Default
    private List<TicketTypeDto> ticketTypes = new ArrayList<>();
    
    @Builder.Default
    private List<RegistrationDto> registrations = new ArrayList<>();

    /**
     * Convert Event entity to EventResponse DTO.
     *
     * @param event The event entity
     * @return EventResponse DTO
     */
    public static EventResponse fromEntity(Event event) {
        EventResponse response = EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .organizerId(event.getOrganizerId())
                .organizerName(event.getOrganizerName())
                .status(event.getStatus())
                .category(event.getCategory())
                .imageUrl(event.getImageUrl())
                .additionalInfo(event.getAdditionalInfo())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();

        // Map speakers
        if (event.getSpeakers() != null) {
            response.setSpeakers(event.getSpeakers().stream()
                    .map(speaker -> SpeakerDto.builder()
                            .id(speaker.getId())
                            .name(speaker.getName())
                            .bio(speaker.getBio())
                            .photoUrl(speaker.getPhotoUrl())
                            .company(speaker.getCompany())
                            .jobTitle(speaker.getJobTitle())
                            .socialLinks(speaker.getSocialLinks())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map agenda items and sessions
        if (event.getAgenda() != null) {
            response.setAgenda(event.getAgenda().stream()
                    .map(agendaItem -> {
                        AgendaItemDto agendaItemDto = AgendaItemDto.builder()
                                .id(agendaItem.getId())
                                .title(agendaItem.getTitle())
                                .description(agendaItem.getDescription())
                                .startTime(agendaItem.getStartTime())
                                .endTime(agendaItem.getEndTime())
                                .type(agendaItem.getType())
                                .build();

                        if (agendaItem.getSessions() != null) {
                            agendaItemDto.setSessions(agendaItem.getSessions().stream()
                                    .map(session -> SessionDto.builder()
                                            .id(session.getId())
                                            .title(session.getTitle())
                                            .description(session.getDescription())
                                            .location(session.getLocation())
                                            .startTime(session.getStartTime())
                                            .endTime(session.getEndTime())
                                            .speakerIds(session.getSpeakerIds())
                                            .capacity(session.getCapacity())
                                            .sessionType(session.getSessionType())
                                            .additionalInfo(session.getAdditionalInfo())
                                            .build())
                                    .collect(Collectors.toList()));
                        }

                        return agendaItemDto;
                    })
                    .collect(Collectors.toList()));
        }

        // Map ticket types
        if (event.getTicketTypes() != null) {
            response.setTicketTypes(event.getTicketTypes().stream()
                    .map(ticketType -> TicketTypeDto.builder()
                            .id(ticketType.getId())
                            .name(ticketType.getName())
                            .description(ticketType.getDescription())
                            .price(ticketType.getPrice())
                            .quantity(ticketType.getQuantity())
                            .sold(ticketType.getSold())
                            .saleStartDate(ticketType.getSaleStartDate())
                            .saleEndDate(ticketType.getSaleEndDate())
                            .available(ticketType.isAvailable())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map registrations
        if (event.getRegistrations() != null) {
            response.setRegistrations(event.getRegistrations().stream()
                    .map(registration -> RegistrationDto.builder()
                            .id(registration.getId())
                            .userId(registration.getUserId())
                            .userName(registration.getUserName())
                            .userEmail(registration.getUserEmail())
                            .ticketTypeId(registration.getTicketTypeId())
                            .ticketTypeName(registration.getTicketTypeName())
                            .amountPaid(registration.getAmountPaid())
                            .status(registration.getStatus())
                            .registrationDate(registration.getRegistrationDate())
                            .confirmationCode(registration.getConfirmationCode())
                            .sessionIds(registration.getSessionIds())
                            .attendeeInfo(registration.getAttendeeInfo())
                            .build())
                    .collect(Collectors.toList()));
        }

        return response;
    }

    /**
     * DTO for speaker information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpeakerDto {
        private String id;
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
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String type;
        
        @Builder.Default
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
        private String title;
        private String description;
        private String location;
        private LocalDateTime startTime;
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
        private String name;
        private String description;
        private double price;
        private int quantity;
        private int sold;
        private LocalDateTime saleStartDate;
        private LocalDateTime saleEndDate;
        private boolean available;
    }

    /**
     * DTO for registration information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationDto {
        private String id;
        private String userId;
        private String userName;
        private String userEmail;
        private String ticketTypeId;
        private String ticketTypeName;
        private double amountPaid;
        private String status;
        private LocalDateTime registrationDate;
        private String confirmationCode;
        private List<String> sessionIds;
        private Map<String, String> attendeeInfo;
    }
}