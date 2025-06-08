package org.example.ticketservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    private UUID ticketId;
    @Column(nullable = false)
    private BigDecimal price;
    private TicketType ticketType;
    @Column(nullable = false)
    private UUID eventId;
    private TicketStatus ticketStatus;
    private LocalDateTime expirationDate;
    private String seatNumber;
}

