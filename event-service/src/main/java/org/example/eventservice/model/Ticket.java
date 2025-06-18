package org.example.eventservice.model;

import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ticketId;
    @Column(nullable = false)
    private Double price;
    @NotNull(message = "Ticket type cannot be null")
    @NotBlank(message = "Ticket type cannot be blank")
    private String ticketType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", nullable = false)
    private Event event;
    private LocalDateTime expirationDate;
    private TicketStatus ticketStatus;
}

