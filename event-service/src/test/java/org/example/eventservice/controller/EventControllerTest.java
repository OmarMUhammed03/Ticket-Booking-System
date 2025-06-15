package org.example.eventservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventservice.dto.AddTicketsDto;
import org.example.eventservice.dto.CreateEventDto;
import org.example.eventservice.dto.EventResponseDto;
import org.example.eventservice.service.EventService;
import org.example.eventservice.service.VenueService;
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

@WebMvcTest(EventController.class)
class EventControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private EventService eventService;
    @MockBean
    private VenueService venueService;

    @Test
    @DisplayName("Should return all events")
    void testGetAllEvents() throws Exception {
        Mockito.when(eventService.getAllEvents()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return event by ID if found")
    void testGetEventByIdFound() throws Exception {
        UUID id = UUID.randomUUID();
        EventResponseDto response = new EventResponseDto();
        Mockito.when(eventService.getEventById(id)).thenReturn(Optional.of(response));
        mockMvc.perform(get("/events/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if event by ID not found")
    void testGetEventByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(eventService.getEventById(id)).thenReturn(Optional.empty());
        mockMvc.perform(get("/events/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create event and return it if successful")
    void testCreateEventSuccess() throws Exception {
        CreateEventDto dto = new CreateEventDto();
        EventResponseDto response = new EventResponseDto();
        UUID userId = UUID.randomUUID();
        Mockito.when(eventService.createEvent(any(), any(), anyString())).thenReturn(Optional.of(response));
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("X-User-Roles", "ADMIN")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 if event creation fails")
    void testCreateEventFail() throws Exception {
        CreateEventDto dto = new CreateEventDto();
        UUID userId = UUID.randomUUID();
        Mockito.when(eventService.createEvent(any(), any(), anyString())).thenReturn(Optional.empty());
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("X-User-Roles", "ADMIN")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete event if found")
    void testDeleteEventFound() throws Exception {
        UUID id = UUID.randomUUID();
        EventResponseDto response = new EventResponseDto();
        Mockito.when(eventService.deleteEvent(id)).thenReturn(Optional.of(response));
        mockMvc.perform(delete("/events/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if event to delete not found")
    void testDeleteEventNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(eventService.deleteEvent(id)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/events/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create tickets for event if found")
    void testCreateTicketsForEventFound() throws Exception {
        UUID id = UUID.randomUUID();
        AddTicketsDto dto = new AddTicketsDto();
        Mockito.when(eventService.createTicketsForEvent(eq(id), anyString(), any())).thenReturn(Optional.of(10));
        mockMvc.perform(post("/events/" + id + "/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Roles", "ADMIN")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if event to create tickets for not found")
    void testCreateTicketsForEventNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        AddTicketsDto dto = new AddTicketsDto();
        Mockito.when(eventService.createTicketsForEvent(eq(id), anyString(), any())).thenReturn(Optional.empty());
        mockMvc.perform(post("/events/" + id + "/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Roles", "ADMIN")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return ticket by ID if found")
    void testGetTicketByIdFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        EventResponseDto.TicketResponseDto ticket = new EventResponseDto.TicketResponseDto();
        Mockito.when(eventService.getTicketById(eventId, ticketId)).thenReturn(Optional.of(ticket));
        mockMvc.perform(get("/events/" + eventId + "/tickets/" + ticketId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if ticket by ID not found")
    void testGetTicketByIdNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Mockito.when(eventService.getTicketById(eventId, ticketId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/events/" + eventId + "/tickets/" + ticketId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all tickets for event if found")
    void testGetTicketsForEventFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        List<EventResponseDto.TicketResponseDto> tickets = new ArrayList<>();
        Mockito.when(eventService.getTicketsForEvent(eventId)).thenReturn(Optional.of(tickets));
        mockMvc.perform(get("/events/" + eventId + "/tickets"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if tickets for event not found")
    void testGetTicketsForEventNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        Mockito.when(eventService.getTicketsForEvent(eventId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/events/" + eventId + "/tickets"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reserve event ticket if found")
    void testReserveEventTicketFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Mockito.when(eventService.reserveEventTicket(eq(eventId), eq(ticketId), anyString())).thenReturn(Optional.of("reserved"));
        mockMvc.perform(patch("/events/" + eventId + "/tickets/" + ticketId)
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if event ticket to reserve not found")
    void testReserveEventTicketNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Mockito.when(eventService.reserveEventTicket(eq(eventId), eq(ticketId), anyString())).thenReturn(Optional.empty());
        mockMvc.perform(patch("/events/" + eventId + "/tickets/" + ticketId)
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return ticket availability if found")
    void testIsTicketAvailableFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Mockito.when(eventService.isTicketAvailable(eventId, ticketId)).thenReturn(Optional.of(true));
        mockMvc.perform(get("/events/" + eventId + "/tickets/" + ticketId + "/available"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 if ticket availability not found")
    void testIsTicketAvailableNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Mockito.when(eventService.isTicketAvailable(eventId, ticketId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/events/" + eventId + "/tickets/" + ticketId + "/available"))
                .andExpect(status().isNotFound());
    }
}

