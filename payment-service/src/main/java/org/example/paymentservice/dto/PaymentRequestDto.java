package org.example.paymentservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentRequestDto {
    private UUID bookingId;
}

