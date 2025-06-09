package org.example.eventservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTicketsDto {
    @NotNull(message = "Event ID cannot be null")
    private Long eventId;
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;
    @NotNull(message = "Ticket type cannot be null")
    @NotBlank(message = "Ticket type cannot be blank")
    private String ticketType;
    @NotNull(message = "Price cannot be null")
    private Double price;
}
