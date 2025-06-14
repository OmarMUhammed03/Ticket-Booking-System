package org.example.apigateway.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingResponseDto {
    private UUID id;
    private UUID userId;
    private UUID eventId;
    private UUID ticketId;
    private String bookingStatus;
    private LocalDateTime bookingDate;
    private String bookingDetail;
}

