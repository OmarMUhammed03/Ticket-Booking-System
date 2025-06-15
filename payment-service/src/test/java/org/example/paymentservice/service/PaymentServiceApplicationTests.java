package org.example.paymentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.repository.PaymentRepository;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingId = UUID.randomUUID();
    }

    @AfterEach
    void reset() {
        paymentRepository.deleteAll();
    }

    @Test
    void testCreatePaymentIntegration() throws Exception {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setBookingId(bookingId);
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId.toString()));
    }

    @Test
    void testGetPaymentByIdIntegration() throws Exception {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(bookingId);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        mockMvc.perform(get("/payments/" + payment.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(payment.getPaymentId().toString()));
    }

    @Test
    void testGetAllPaymentsIntegration() throws Exception {
        Payment payment1 = new Payment();
        payment1.setPaymentId(UUID.randomUUID());
        payment1.setBookingId(bookingId);
        payment1.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment1);
        Payment payment2 = new Payment();
        payment2.setPaymentId(UUID.randomUUID());
        payment2.setBookingId(UUID.randomUUID());
        payment2.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment2);
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").exists())
                .andExpect(jsonPath("$[1].paymentId").exists());
    }

    @Test
    void testUpdatePaymentIntegration() throws Exception {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(bookingId);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setBookingId(bookingId);
        mockMvc.perform(put("/payments/" + payment.getPaymentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(payment.getPaymentId().toString()));
    }

    @Test
    void testDeletePaymentIntegration() throws Exception {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(bookingId);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        mockMvc.perform(delete("/payments/" + payment.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(payment.getPaymentId().toString()));
        assert (paymentRepository.findById(payment.getPaymentId())).isEmpty();
    }
}