package org.example.paymentservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponseDto {
    private UUID paymentId;
    private UUID bookingId;
    private LocalDateTime paymentDate;
}

