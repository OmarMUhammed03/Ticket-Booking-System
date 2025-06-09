package org.example.eventservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventDto {
    @NotBlank(message = "Event name is required")
    private String name;

    @NotNull(message = "Start date/time is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date/time is required")
    private LocalDateTime endDate;

    private String description;

    @NotNull(message = "Creator ID is required")
    private UUID creatorId;

    @NotNull(message = "Venue ID is required")
    private Long venueId;
}
