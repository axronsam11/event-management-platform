package com.eventmanagement.api.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for event registration requests.
 * Contains all fields needed for registering to an event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotBlank(message = "Ticket type ID is required")
    private String ticketTypeId;

    @NotNull(message = "Amount paid is required")
    private double amountPaid;

    @Builder.Default
    private List<String> sessionIds = new ArrayList<>();

    @Builder.Default
    private Map<String, String> attendeeInfo = Map.of();
}