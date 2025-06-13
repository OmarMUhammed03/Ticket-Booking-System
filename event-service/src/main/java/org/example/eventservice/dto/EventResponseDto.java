package org.example.eventservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EventResponseDto {

    private UUID id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;

    private VenueDto venue;

    private List<TicketResponseDto> tickets;


    @Data
    public static class VenueDto {
        private UUID venueId;
        private String name;
        private String address;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String contactPhone;
        private String contactEmail;
    }

    @Data
    public static class TicketResponseDto {
        private UUID ticketId;
        private Double price;
        private String ticketType;
        private String ticketStatus;
        private LocalDateTime expirationDate;
    }
}
