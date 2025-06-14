package org.example.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBookingResponseDto {
    private String userFirstname;
    private String userLastname;
    private String userEmail;
    private UUID bookingId;
    private String eventName;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;
    private String venueName;
    private String venueAddress;
    private String venueCity;
    private String venueState;
    private String venueCountry;
    private String ticketType;
    private String bookingStatus;
    private LocalDateTime bookingDateTime;
}

