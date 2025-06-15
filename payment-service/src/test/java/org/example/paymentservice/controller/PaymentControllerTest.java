package org.example.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Should create a payment and return the created payment")
    void testCreatePayment() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto();
        PaymentResponseDto response = new PaymentResponseDto();
        Mockito.when(paymentService.createPayment(any())).thenReturn(response);
        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return payment by ID if found")
    void testGetPaymentByIdFound() throws Exception {
        UUID id = UUID.randomUUID();
        PaymentResponseDto response = new PaymentResponseDto();
        Mockito.when(paymentService.getPaymentById(id)).thenReturn(Optional.of(response));
        mockMvc.perform(get("/payments/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if payment by ID not found")
    void testGetPaymentByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(paymentService.getPaymentById(id)).thenReturn(Optional.empty());
        mockMvc.perform(get("/payments/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all payments")
    void testGetAllPayments() throws Exception {
        Mockito.when(paymentService.getAllPayments()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should update payment if found")
    void testUpdatePaymentFound() throws Exception {
        UUID id = UUID.randomUUID();
        PaymentRequestDto request = new PaymentRequestDto();
        PaymentResponseDto response = new PaymentResponseDto();
        Mockito.when(paymentService.updatePayment(eq(id), any())).thenReturn(Optional.of(response));
        mockMvc.perform(put("/payments/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if payment to update not found")
    void testUpdatePaymentNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        PaymentRequestDto request = new PaymentRequestDto();
        Mockito.when(paymentService.updatePayment(eq(id), any())).thenReturn(Optional.empty());
        mockMvc.perform(put("/payments/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete payment if found")
    void testDeletePaymentFound() throws Exception {
        UUID id = UUID.randomUUID();
        PaymentResponseDto response = new PaymentResponseDto();
        Mockito.when(paymentService.deletePayment(id)).thenReturn(Optional.of(response));
        mockMvc.perform(delete("/payments/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if payment to delete not found")
    void testDeletePaymentNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(paymentService.deletePayment(id)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/payments/" + id))
                .andExpect(status().isNotFound());
    }
}

