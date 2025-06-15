package org.example.apigateway.controller;

import org.example.apigateway.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;

@WebFluxTest(ApiGatewayController.class)
class ApiGatewayControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Should return empty booking history if no bookings exist")
    void testGetBookingHistoryEmpty() {
        Mockito.when(restTemplate.exchange(
                contains("/bookings/current-user"), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))
        ).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        webTestClient.get().uri("/my-account/booking-history")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json("[]");
    }

    @Test
    @DisplayName("Should return booking history with events if bookings exist")
    void testGetBookingHistoryWithEvents() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID venueId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EventResponseDto.VenueDto venue = new EventResponseDto.VenueDto();
        venue.setVenueId(venueId);
        venue.setName("Test Venue");
        venue.setAddress("123 Main St");
        venue.setCity("Test City");
        venue.setState("TS");
        venue.setCountry("USA");

        EventResponseDto event = new EventResponseDto();
        event.setId(eventId);
        event.setName("Test Event");
        event.setStartDate(LocalDate.now().atStartOfDay());
        event.setEndDate(LocalDate.now().plusDays(1).atStartOfDay());
        event.setVenue(venue);

        UserResponseDto user = new UserResponseDto();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDateOfBirth(LocalDate.now().minusYears(30));
        user.setGender("Male");

        EventResponseDto.TicketResponseDto ticket = new EventResponseDto.TicketResponseDto();
        ticket.setTicketId(ticketId);
        ticket.setTicketType("VIP");
        ticket.setPrice(100.00);

        BookingResponseDto booking = new BookingResponseDto();
        booking.setId(bookingId);
        booking.setBookingDate(LocalDateTime.now());
        booking.setTicketId(ticketId);
        booking.setEventId(eventId);
        booking.setUserId(userId);

        Mockito.when(restTemplate.exchange(
                contains("/bookings/current-user"), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))
        ).thenReturn(ResponseEntity.ok(List.of(booking)));

        Mockito.when(restTemplate.exchange(
                contains("/events?ids="), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))
        ).thenReturn(ResponseEntity.ok(List.of(event)));

        Mockito.when(restTemplate.exchange(
                contains("/users/current-user"), eq(HttpMethod.GET), any(), eq(UserResponseDto.class))
        ).thenReturn(ResponseEntity.ok(user));

        Mockito.when(restTemplate.exchange(
                contains("/events/" + eventId + "/tickets/" + ticketId), eq(HttpMethod.GET), any(), eq(EventResponseDto.TicketResponseDto.class))
        ).thenReturn(ResponseEntity.ok(ticket));

        webTestClient.get().uri("/my-account/booking-history")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].eventName").isEqualTo("Test Event")
                .jsonPath("$[0].userEmail").isEqualTo("test@example.com")
                .jsonPath("$[0].ticketType").isEqualTo("VIP");
    }

    @Test
    @DisplayName("Should handle error from booking service gracefully")
    void testGetBookingHistoryBookingServiceError() {
        Mockito.when(restTemplate.exchange(
                anyString(), any(), any(), any(ParameterizedTypeReference.class))
        ).thenThrow(new RuntimeException("Booking service error"));

        webTestClient.get().uri("/my-account/booking-history")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}