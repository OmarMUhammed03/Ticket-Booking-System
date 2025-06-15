package org.example.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookingservice.dto.BookingRequestDto;
import org.example.bookingservice.dto.BookingResponseDto;
import org.example.bookingservice.service.BookingService;
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

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("Should create a booking and return the created booking")
    void testCreateBooking() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        BookingResponseDto response = new BookingResponseDto();
        UUID userId = UUID.randomUUID();
        Mockito.when(bookingService.createBooking(any(), any(), anyString())).thenReturn(response);
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("Authorization", "Bearer token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return booking by ID if found")
    void testGetBookingByIdFound() throws Exception {
        UUID id = UUID.randomUUID();
        BookingResponseDto response = new BookingResponseDto();
        Mockito.when(bookingService.getBookingById(id)).thenReturn(Optional.of(response));
        mockMvc.perform(get("/bookings/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if booking by ID not found")
    void testGetBookingByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(bookingService.getBookingById(id)).thenReturn(Optional.empty());
        mockMvc.perform(get("/bookings/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all bookings")
    void testGetAllBookings() throws Exception {
        Mockito.when(bookingService.getAllBookings()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should update booking if found")
    void testUpdateBookingFound() throws Exception {
        UUID id = UUID.randomUUID();
        BookingRequestDto request = new BookingRequestDto();
        BookingResponseDto response = new BookingResponseDto();
        UUID userId = UUID.randomUUID();
        Mockito.when(bookingService.updateBooking(eq(id), any(), eq(userId))).thenReturn(Optional.of(response));
        mockMvc.perform(put("/bookings/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if booking to update not found")
    void testUpdateBookingNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        BookingRequestDto request = new BookingRequestDto();
        UUID userId = UUID.randomUUID();
        Mockito.when(bookingService.updateBooking(eq(id), any(), eq(userId))).thenReturn(Optional.empty());
        mockMvc.perform(put("/bookings/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete booking if found")
    void testDeleteBookingFound() throws Exception {
        UUID id = UUID.randomUUID();
        BookingResponseDto response = new BookingResponseDto();
        Mockito.when(bookingService.deleteBooking(id)).thenReturn(Optional.of(response));
        mockMvc.perform(delete("/bookings/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if booking to delete not found")
    void testDeleteBookingNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(bookingService.deleteBooking(id)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/bookings/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return bookings by user ID")
    void testGetBookingsByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(bookingService.getBookingsByUserId(any(), any(), anyString())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/bookings/user/" + userId)
                        .header("X-User-Id", userId)
                        .header("X-User-Roles", "USER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return bookings for current user")
    void testGetCurrentUserBookings() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(bookingService.getBookingsByUserId(userId)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/bookings/current-user")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());
    }
}

