package org.example.eventservice.controller;

import org.example.eventservice.dto.AddTicketsDto;
import org.example.eventservice.dto.CreateEventDto;
import org.example.eventservice.dto.EventResponseDto;
import org.example.eventservice.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EventController.class);

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        LOGGER.info("Fetching all events");
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable("id") UUID eventId) {
        LOGGER.info("Fetching event with ID: {}", eventId);
        Optional<EventResponseDto> event = eventService.getEventById(eventId);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody CreateEventDto dto,
                                                        @RequestHeader("X-User-Id") UUID userId,
                                                        @RequestHeader("X-User-Roles") String userRole) {
        LOGGER.info("Creating event with name: {}", dto.getName());
        Optional<EventResponseDto> event = eventService.createEvent(dto, userId, userRole);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EventResponseDto> deleteEvent(@PathVariable("id") UUID eventId) {
        LOGGER.info("Deleting event with ID: {}", eventId);
        Optional<EventResponseDto> event = eventService.deleteEvent(eventId);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/tickets")
    public ResponseEntity<?> createTicketsForEvent(@PathVariable("id") UUID eventId,
                                                   @RequestHeader("X-User-Roles") String userRole,
                                                   @RequestBody AddTicketsDto dto) {
        LOGGER.info("Creating tickets for event with ID: {}", eventId);
        Optional<Integer> event = eventService.createTicketsForEvent(eventId, userRole, dto);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/tickets/{ticketId}")
    public ResponseEntity<EventResponseDto.TicketResponseDto> getTicketById(@PathVariable("id") UUID eventId,
                                           @PathVariable("ticketId") UUID ticketId) {
        LOGGER.info("Fetching ticket with ID: {} for event with ID: {}", ticketId, eventId);
        Optional<EventResponseDto.TicketResponseDto> ticket = eventService.getTicketById(eventId, ticketId);
        return ticket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<?> getTicketsForEvent(@PathVariable("id") UUID eventId) {
        LOGGER.info("Fetching tickets for event with ID: {}", eventId);
        Optional<List<EventResponseDto.TicketResponseDto>> tickets = eventService.getTicketsForEvent(eventId);
        return tickets.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/tickets/{ticketId}")
    public ResponseEntity<?> reserveEventTicket(@PathVariable("id") UUID eventId,
                                                @PathVariable("ticketId") UUID ticketId,
                                                @RequestHeader("X-User-Id") String userId) {
        LOGGER.info("Updating ticket with ID: {} for event with ID: {}", ticketId, eventId);
        Optional<String> updatedTicket =
                eventService.reserveEventTicket(eventId, ticketId, userId);
        return updatedTicket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tickets/{ticketId}/available")
    public ResponseEntity<Boolean> isTicketAvailable(@PathVariable("id") UUID eventId,
                                                @PathVariable("ticketId") UUID ticketId) {
        LOGGER.info("Checking the availability of the ticket with ID: {} for event with ID: {}", ticketId, eventId);
        Optional<Boolean> updatedTicket =
                eventService.isTicketAvailable(eventId, ticketId);
        return updatedTicket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping(params = "ids")
    public ResponseEntity<List<EventResponseDto>> getEventsByIds(@RequestParam("ids") List<UUID> eventIds) {
        LOGGER.info("Fetching events with IDs: {}", eventIds);
        List<EventResponseDto> events = eventService.getEventsByIds(eventIds);
        return ResponseEntity.ok(events);
    }
}