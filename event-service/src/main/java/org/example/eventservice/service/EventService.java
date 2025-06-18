package org.example.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.example.commonlibrary.InvalidActionException;
import org.example.commonlibrary.NotFoundException;
import org.example.commonlibrary.ValidationException;
import org.example.eventservice.dto.AddTicketsDto;
import org.example.eventservice.dto.CreateEventDto;
import org.example.eventservice.dto.EventResponseDto;
import org.example.eventservice.model.Venue;
import org.example.eventservice.model.Event;
import org.example.eventservice.model.Ticket;
import org.example.eventservice.model.TicketStatus;
import org.example.eventservice.mapper.EventMapper;
import org.example.eventservice.repository.EventRepository;
import org.example.eventservice.repository.TicketRepository;
import org.example.eventservice.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final VenueRepository venueRepository;

    @Value("${ticket.expiration.duration.minutes}")
    private Integer ticketExpirationDurationMinutes;

    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(EventMapper::toDto)
                .toList();
    }

    public Optional<EventResponseDto> getEventById(final UUID id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            throw new NotFoundException("Event Not Found");
        }
        return Optional.of(EventMapper.toDto(event.get()));
    }

    public Optional<EventResponseDto> createEvent(final CreateEventDto dto, final UUID creatorId,
                                                  final String userRole) {
        if (userRole == null || (!userRole.equals("ADMIN") && !userRole.equals("ORGANIZER"))) {
            throw new InvalidActionException("Only admins and organizers can create events");
        }
        Optional<Venue> venue = venueRepository.findById(dto.getVenueId());
        if (venue.isEmpty()) {
            throw new NotFoundException("Venue Not Found");
        }
        Venue existingVenue = venue.get();
        Event event = EventMapper.toEntity(dto, creatorId, existingVenue);
        event.setVenue(existingVenue);
        existingVenue.addEvent(event);
        Event savedEvent = eventRepository.save(event);
        venueRepository.save(existingVenue);
        return Optional.of(EventMapper.toDto(savedEvent));
    }

    public Optional<EventResponseDto> deleteEvent(final UUID id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            eventRepository.delete(event);
            return Optional.of(EventMapper.toDto(event));
        }
        throw new NotFoundException("Event Not Found");
    }

    public Optional<Integer> createTicketsForEvent(final UUID eventId, final String userRole, final AddTicketsDto dto) {
        if (userRole == null || (!userRole.equals("ADMIN") && !userRole.equals("ORGANIZER"))) {
            throw new InvalidActionException("Only admins or organizers can create tickets for events");
        }
        if (dto.getQuantity() <= 0) {
            throw new ValidationException("Invalid ticket quantity");
        }
        if (dto.getPrice() <= 0) {
            throw new ValidationException("Invalid ticket price");
        }
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event Not Found");
        }
        Event event = eventOptional.get();
        for (int i = 0; i < dto.getQuantity(); i++) {
            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setPrice(dto.getPrice());
            ticket.setTicketType(dto.getTicketType());
            ticket.setTicketStatus(TicketStatus.AVAILABLE);
            ticket.setEvent(event);
            event.addTicket(ticket);

            ticketRepository.save(ticket);
        }
        eventRepository.save(event);
        return Optional.of(dto.getQuantity());
    }

    public Optional<List<EventResponseDto.TicketResponseDto>> getTicketsForEvent(final UUID eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event Not Found");
        }
        Event event = eventOptional.get();
        List<EventResponseDto.TicketResponseDto> ticketDtos = event.getTickets()
                .stream().filter(t -> (t.getTicketStatus() == TicketStatus.AVAILABLE
                        || (t.getTicketStatus() == TicketStatus.RESERVED && t.getExpirationDate() != null
                                && t.getExpirationDate().isAfter(LocalDateTime.now()))))
                .map(EventMapper::mapTicketToDto)
                .toList();
        return Optional.of(ticketDtos);
    }

    private boolean availableTicket(final Ticket ticket) {
        return ticket.getTicketStatus() == TicketStatus.AVAILABLE
                || (ticket.getTicketStatus() == TicketStatus.RESERVED
                && ticket.getExpirationDate() != null && ticket.getExpirationDate().isAfter(LocalDateTime.now()));
    }

    private void validTicket(final UUID eventId, final UUID ticketId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event Not Found");
        }
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            throw new NotFoundException("Ticket Not Found");
        }
        Ticket ticket = ticketOptional.get();
        if (!ticket.getEvent().getId().equals(eventId)) {
            throw new ValidationException("Ticket does not belong to the specified event");
        }
    }

    public Optional<String> reserveEventTicket(final UUID eventId, final UUID ticketId, final String userId) {
        isTicketAvailable(eventId, ticketId);
        Ticket ticket = ticketRepository.findById(ticketId).get();
        ticket.setTicketStatus(TicketStatus.RESERVED);
        ticket.setExpirationDate(LocalDateTime.now().plusMinutes(ticketExpirationDurationMinutes));
        ticketRepository.save(ticket);
        return Optional.of("Ticket reserved successfully");
    }

    public Optional<Boolean> isTicketAvailable(final UUID eventId, final UUID ticketId) {
        validTicket(eventId, ticketId);
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        Ticket ticket = ticketOptional.get();
        if (!availableTicket(ticket)) {
            throw new InvalidActionException("Ticket is not available for reservation");
        }
        return Optional.of(true);
    }

    public List<EventResponseDto> getEventsByIds(final List<UUID> eventIds) {
        List<Event> events = eventRepository.findAllById(eventIds);
        Set<UUID> foundIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Set<UUID> missingIds = new HashSet<>(eventIds);
        missingIds.removeAll(foundIds);
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Events not found for IDs: " + missingIds);
        }
        return events.stream()
                .map(EventMapper::toDto)
                .toList();
    }

    public Optional<EventResponseDto.TicketResponseDto> getTicketById(final UUID eventId, final UUID ticketId) {
        validTicket(eventId, ticketId);
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        Ticket ticket = ticketOptional.get();
        return Optional.of(EventMapper.mapTicketToDto(ticket));
    }
}
