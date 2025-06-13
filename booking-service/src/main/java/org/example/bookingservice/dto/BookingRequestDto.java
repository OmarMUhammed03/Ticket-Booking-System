package org.example.bookingservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingRequestDto {
    private UUID eventId;
    private UUID ticketId;
    private String bookingStatus;
    private LocalDateTime bookingDate;
    private String bookingDetail;
}

