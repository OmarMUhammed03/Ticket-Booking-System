package org.example.eventservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.eventservice.dto.AddTicketsDto;
import org.example.eventservice.dto.CreateEventDto;
import org.example.eventservice.dto.EventResponseDto;
import org.example.eventservice.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@Tag(name = "Event Controller", description = "Endpoints for managing events")
public class EventController {
    private final EventService eventService;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EventController.class);

    public EventController(final EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Get all events", description = "Fetches all event records.")
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        LOGGER.info("Fetching all events");
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @Operation(summary = "Get event by ID", description = "Retrieves an event record by its unique identifier.")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable("id") final UUID eventId) {
        LOGGER.info("Fetching event with ID: {}", eventId);
        Optional<EventResponseDto> event = eventService.getEventById(eventId);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new event",
            description = "Creates a new event record based on the provided request data.")
    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody final CreateEventDto dto,
                                                        @RequestHeader("X-User-Id") final UUID userId,
                                                        @RequestHeader("X-User-Roles") final String userRole) {
        LOGGER.info("Creating event with name: {}", dto.getName());
        Optional<EventResponseDto> event = eventService.createEvent(dto, userId, userRole);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Delete an event", description = "Deletes an event record by its unique identifier.")
    @DeleteMapping("/{id}")
    public ResponseEntity<EventResponseDto> deleteEvent(@PathVariable("id") final UUID eventId) {
        LOGGER.info("Deleting event with ID: {}", eventId);
        Optional<EventResponseDto> event = eventService.deleteEvent(eventId);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/tickets")
    public ResponseEntity<?> createTicketsForEvent(@PathVariable("id") final UUID eventId,
                                                   @RequestHeader("X-User-Roles") final String userRole,
                                                   @RequestBody final AddTicketsDto dto) {
        LOGGER.info("Creating tickets for event with ID: {}", eventId);
        Optional<Integer> event = eventService.createTicketsForEvent(eventId, userRole, dto);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tickets/{ticketId}")
    public ResponseEntity<EventResponseDto.TicketResponseDto> getTicketById(@PathVariable("id") final UUID eventId,
                                                                            @PathVariable("ticketId") final
                                                                            UUID ticketId) {
        LOGGER.info("Fetching ticket with ID: {} for event with ID: {}", ticketId, eventId);
        Optional<EventResponseDto.TicketResponseDto> ticket = eventService.getTicketById(eventId, ticketId);
        return ticket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<?> getTicketsForEvent(@PathVariable("id") final UUID eventId) {
        LOGGER.info("Fetching tickets for event with ID: {}", eventId);
        Optional<List<EventResponseDto.TicketResponseDto>> tickets = eventService.getTicketsForEvent(eventId);
        return tickets.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/tickets/{ticketId}")
    public ResponseEntity<?> reserveEventTicket(@PathVariable("id") final UUID eventId,
                                                @PathVariable("ticketId") final UUID ticketId,
                                                @RequestHeader("X-User-Id") final String userId) {
        LOGGER.info("Updating ticket with ID: {} for event with ID: {}", ticketId, eventId);
        Optional<String> updatedTicket =
                eventService.reserveEventTicket(eventId, ticketId, userId);
        return updatedTicket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tickets/{ticketId}/available")
    public ResponseEntity<Boolean> isTicketAvailable(@PathVariable("id") final UUID eventId,
                                                     @PathVariable("ticketId") final UUID ticketId) {
        LOGGER.info("Checking the availability of the ticket with ID: {} for event with ID: {}", ticketId, eventId);
        Optional<Boolean> updatedTicket =
                eventService.isTicketAvailable(eventId, ticketId);
        return updatedTicket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(params = "ids")
    public ResponseEntity<List<EventResponseDto>> getEventsByIds(@RequestParam("ids") final List<UUID> eventIds) {
        LOGGER.info("Fetching events with IDs: {}", eventIds);
        List<EventResponseDto> events = eventService.getEventsByIds(eventIds);
        return ResponseEntity.ok(events);
    }
}
