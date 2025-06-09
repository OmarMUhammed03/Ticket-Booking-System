package org.example.eventservice.mapper;

import org.example.eventservice.dto.CreateEventDto;
import org.example.eventservice.dto.EventResponseDto;
import org.example.eventservice.model.Event;
import org.example.eventservice.model.Ticket;
import org.example.eventservice.model.Venue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public static EventResponseDto toDto(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setDescription(event.getDescription());

        Venue v = event.getVenue();
        EventResponseDto.VenueDto venueDto = new EventResponseDto.VenueDto();
        venueDto.setVenueId(v.getVenueId());
        venueDto.setName(v.getName());
        venueDto.setAddress(v.getAddress());
        venueDto.setCity(v.getCity());
        venueDto.setState(v.getState());
        venueDto.setPostalCode(v.getPostalCode());
        venueDto.setCountry(v.getCountry());
        venueDto.setContactPhone(v.getContactPhone());
        venueDto.setContactEmail(v.getContactEmail());
        dto.setVenue(venueDto);

        List<EventResponseDto.TicketResponseDto> ticketDtos =
                event.getTickets()
                        .stream()
                        .map(EventMapper::mapTicketToDto)
                        .collect(Collectors.toList());
        dto.setTickets(ticketDtos);

        return dto;
    }

    public static EventResponseDto.TicketResponseDto mapTicketToDto(Ticket t) {
        EventResponseDto.TicketResponseDto tDto = new EventResponseDto.TicketResponseDto();
        tDto.setTicketId(t.getTicketId());
        tDto.setPrice(t.getPrice());
        tDto.setTicketType(t.getTicketType());
        tDto.setTicketStatus(String.valueOf(t.getTicketStatus()));
        return tDto;
    }

    public static Event toEntity(CreateEventDto dto, UUID creatorId, Venue venue) {
        Event event = new Event();

        event.setName(dto.getName());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setDescription(dto.getDescription());
        event.setCreatorId(creatorId);
        event.setVenue(venue);

        return event;
    }
}
