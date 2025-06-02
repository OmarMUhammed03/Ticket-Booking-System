package org.example.paymentservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.bookingservice.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private UUID paymentId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingId", referencedColumnName = "bookingId", nullable = false, unique = true)
    private Booking booking;
    @Column(nullable = false)
    private BigDecimal amount;
    private LocalDateTime paymentDate;
}

