package org.example.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.apigateway.dto.BookingResponseDto;
import org.example.apigateway.dto.EventResponseDto;
import org.example.apigateway.dto.UserBookingResponseDto;
import  org.example.apigateway.dto.UserResponseDto;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/my-account")
@Tag(name = "API Gateway Controller", description = "Endpoints for managing API Gateway operations")
public class ApiGatewayController {

    private final RestTemplate restTemplate;
    private final String gatewayUrl = "http://localhost:8088/api";

    public ApiGatewayController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Operation(summary = "Get booking history", description = "Fetches the booking history for the current user.")
    @GetMapping("/booking-history")
    public List<UserBookingResponseDto> getBookingHistory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorization) {

        String bookingServiceUrl = gatewayUrl + "/bookings/current-user";
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);

        ResponseEntity<List<BookingResponseDto>> bookingsResponse = restTemplate.exchange(
                bookingServiceUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                }
        );
        List<BookingResponseDto> bookings = bookingsResponse.getBody();
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> eventIds = bookings.stream()
                .map(BookingResponseDto::getEventId)
                .distinct()
                .toList();
        String eventServiceUrl = gatewayUrl + "/events?ids="
                + eventIds.stream()
                        .map(UUID::toString)
                        .collect(Collectors.joining(","));
        ResponseEntity<List<EventResponseDto>> eventsResponse = restTemplate.exchange(
                eventServiceUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                }
        );
        List<EventResponseDto> events = eventsResponse.getBody();
        Map<UUID, EventResponseDto> eventById = (events == null)
                ? Map.of()
                : events.stream().collect(Collectors.toMap(EventResponseDto::getId, e -> e));

        String userServiceUrl = gatewayUrl + "/users/current-user";
        ResponseEntity<UserResponseDto> userResponse = restTemplate.exchange(
                userServiceUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                UserResponseDto.class
        );
        UserResponseDto currentUser = userResponse.getBody();

        List<UserBookingResponseDto> result = new ArrayList<>();
        for (BookingResponseDto booking : bookings) {
            EventResponseDto eventDto = eventById.get(booking.getEventId());

            String ticketServiceUrl = gatewayUrl + "/events/" + eventDto.getId() + "/tickets/" + booking.getTicketId();
            ResponseEntity<EventResponseDto.TicketResponseDto> ticketResponse = restTemplate.exchange(
                    ticketServiceUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    EventResponseDto.TicketResponseDto.class
            );
            EventResponseDto.TicketResponseDto ticket = ticketResponse.getBody();

            UserBookingResponseDto dto = new UserBookingResponseDto();
            dto.setBookingId(booking.getId());
            dto.setEventName(eventDto.getName());
            dto.setEventStartDate(eventDto.getStartDate());
            dto.setEventEndDate(eventDto.getEndDate());
            dto.setVenueName(eventDto.getVenue().getName());
            dto.setVenueAddress(eventDto.getVenue().getAddress());
            dto.setVenueCity(eventDto.getVenue().getCity());
            dto.setVenueState(eventDto.getVenue().getState());
            dto.setVenueCountry(eventDto.getVenue().getCountry());
            dto.setTicketType(ticket.getTicketType());
            dto.setBookingStatus(booking.getBookingStatus());
            dto.setBookingDateTime(booking.getBookingDate());
            dto.setUserEmail(currentUser.getEmail());
            dto.setUserFirstname(currentUser.getFirstName());
            dto.setUserLastname(currentUser.getLastName());
            result.add(dto);
        }
        return result;
    }

}
