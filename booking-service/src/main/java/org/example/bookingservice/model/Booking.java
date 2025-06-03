package org.example.bookingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.eventservice.model.Event;
import org.example.paymentservice.model.Payment;
import org.example.ticketservice.model.Ticket;
import org.example.userservice.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", nullable = false)
    private Event event;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Payment payment;
    private BookingStatus bookingStatus;
    private LocalDateTime bookingDate;
    @Lob
    private String bookingDetail;
}