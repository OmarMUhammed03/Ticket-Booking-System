package org.example.paymentservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private UUID paymentId;
    @Column(nullable = false, unique = true)
    private UUID bookingId;
    private LocalDateTime paymentDate;
}

