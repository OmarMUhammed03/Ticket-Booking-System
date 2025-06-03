package org.example.ticketservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.bookingservice.model.Booking;
import org.example.eventservice.model.Event;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", nullable = false)
    private Event event;
    @OneToOne(mappedBy = "ticket", fetch = FetchType.LAZY)
    private Booking booking;
    private TicketStatus ticketStatus;
    private LocalDateTime expirationDate;
    private String seatNumber;
}

