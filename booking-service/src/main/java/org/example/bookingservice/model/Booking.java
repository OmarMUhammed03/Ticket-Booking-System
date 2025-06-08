package org.example.bookingservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID ticketId;
    private BookingStatus bookingStatus;
    private LocalDateTime bookingDate;
    @Lob
    private String bookingDetail;
}