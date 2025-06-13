package org.example.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.example.commonlibrary.ValidationException;
import org.example.commonlibrary.kafka.MessageProducer;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.mapper.PaymentMapper;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MessageProducer messageProducer;

    public PaymentResponseDto createPayment(PaymentRequestDto paymentRequestDto) {
        if (paymentRequestDto.getBookingId() == null || paymentRequestDto.getBookingId().toString().isEmpty()) {
            throw new ValidationException("Missing Fields");
        }
        Payment payment = PaymentMapper.toPayment(paymentRequestDto);
        Payment saved = paymentRepository.save(payment);
        messageProducer.sendMessage("payment-success", "payment-topic", Map.of(
                "paymentId", saved.getPaymentId().toString(),
                "bookingId", saved.getBookingId().toString()
        ));
        return PaymentMapper.toPaymentResponseDto(saved);
    }

    public Optional<PaymentResponseDto> getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentMapper::toPaymentResponseDto);
    }

    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentMapper::toPaymentResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<PaymentResponseDto> updatePayment(UUID paymentId, PaymentRequestDto paymentRequestDto) {
        return paymentRepository.findById(paymentId).map(existing -> {
            existing.setBookingId(paymentRequestDto.getBookingId());
            Payment saved = paymentRepository.save(existing);
            return PaymentMapper.toPaymentResponseDto(saved);
        });
    }

    public Optional<PaymentResponseDto> deletePayment(UUID paymentId) {
        return paymentRepository.findById(paymentId).map(existing -> {
            paymentRepository.deleteById(paymentId);
            return PaymentMapper.toPaymentResponseDto(existing);
        });
    }

    public List<PaymentResponseDto> getPaymentsByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(PaymentMapper::toPaymentResponseDto)
                .collect(Collectors.toList());
    }
}
