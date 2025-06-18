package org.example.paymentservice.mapper;

import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.model.Payment;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentMapper {
    public static Payment toPayment(final PaymentRequestDto dto) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(dto.getBookingId());
        payment.setPaymentDate(LocalDateTime.now());
        return payment;
    }

    public static PaymentResponseDto toPaymentResponseDto(final Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setPaymentId(payment.getPaymentId());
        dto.setBookingId(payment.getBookingId());
        dto.setPaymentDate(payment.getPaymentDate());
        return dto;
    }
}

