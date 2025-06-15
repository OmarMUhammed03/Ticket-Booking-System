package org.example.eventservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventservice.dto.CreateEventDto;
import org.example.eventservice.dto.CreateVenueDto;
import org.example.eventservice.model.Event;
import org.example.eventservice.model.Ticket;
import org.example.eventservice.model.TicketStatus;
import org.example.eventservice.model.Venue;
import org.example.eventservice.repository.EventRepository;
import org.example.eventservice.repository.TicketRepository;
import org.example.eventservice.repository.VenueRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EventServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Venue testVenue;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        ticketRepository.deleteAll();
        testVenue = new Venue();
        testVenue.setName("Test Venue");
        testVenue.setAddress("123 Main St");
        testVenue.setCity("Test City");
        testVenue.setState("TS");
        testVenue.setCountry("USA");
    }

    @AfterEach
    void reset() {
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(eventRepository).isNotNull();
        assertThat(venueRepository).isNotNull();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void testCreateAndGetEventIntegration() throws Exception {
        venueRepository.save(testVenue);
        CreateEventDto event = new CreateEventDto();
        event.setName("Integration Event");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setDescription("Integration test event");
        event.setVenueId(testVenue.getVenueId());
        UUID creatorId = UUID.randomUUID();

        String eventJson = objectMapper.writeValueAsString(event);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", creatorId.toString())
                        .header("X-User-Roles", "ADMIN")
                        .content(eventJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Event"));

        Event created = eventRepository.findAll().stream().filter(e -> e.getName().equals("Integration Event"))
                .findFirst().orElse(null);
        assertThat(created).isNotNull();
        mockMvc.perform(get("/events/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Event"));
    }

    @Test
    void testGetAllEventsIntegration() throws Exception {
        venueRepository.save(testVenue);
        Event event1 = new Event();
        event1.setName("Event One");
        event1.setStartDate(LocalDateTime.now().plusDays(1));
        event1.setEndDate(LocalDateTime.now().plusDays(2));
        event1.setDescription("Event 1");
        event1.setCreatorId(UUID.randomUUID());
        event1.setVenue(testVenue);
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setName("Event Two");
        event2.setStartDate(LocalDateTime.now().plusDays(3));
        event2.setEndDate(LocalDateTime.now().plusDays(4));
        event2.setDescription("Event 2");
        event2.setCreatorId(UUID.randomUUID());
        event2.setVenue(testVenue);
        eventRepository.save(event2);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Event One"))
                .andExpect(jsonPath("$[1].name").value("Event Two"));
    }

    @Test
    void testDeleteEventIntegration() throws Exception {
        venueRepository.save(testVenue);
        Event event = new Event();
        event.setName("Delete Event");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setDescription("To be deleted");
        event.setCreatorId(UUID.randomUUID());
        event.setVenue(testVenue);
        eventRepository.save(event);

        mockMvc.perform(delete("/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Delete Event"));
        assertThat(eventRepository.findById(event.getId())).isEmpty();
    }

    @Test
    void testGetEventByIdNotFoundIntegration() throws Exception {
        mockMvc.perform(get("/events/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateVenueIntegration() throws Exception {
        CreateVenueDto dto = new CreateVenueDto();
        dto.setName("Venue1");
        dto.setAddress("Addr1");
        dto.setCity("City1");
        dto.setState("State1");
        dto.setPostalCode("12345");
        dto.setCountry("Country1");
        dto.setContactPhone("1234567890");
        dto.setContactEmail("venue1@example.com");
        mockMvc.perform(post("/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Venue1"));
    }

    @Test
    void testGetAllVenuesIntegration() throws Exception {
        Venue v1 = new Venue();
        v1.setVenueId(UUID.randomUUID());
        v1.setName("VenueA");
        v1.setAddress("AddrA");
        v1.setCity("CityA");
        v1.setState("StateA");
        v1.setCountry("CountryA");
        venueRepository.save(v1);
        Venue v2 = new Venue();
        v2.setVenueId(UUID.randomUUID());
        v2.setName("VenueB");
        v2.setAddress("AddrB");
        v2.setCity("CityB");
        v2.setState("StateB");
        v2.setCountry("CountryB");
        venueRepository.save(v2);
        mockMvc.perform(get("/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("VenueA"))
                .andExpect(jsonPath("$[1].name").value("VenueB"));
    }

    @Test
    void testGetVenueByIdIntegration() throws Exception {
        Venue v = new Venue();
        v.setName("VenueX");
        v.setAddress("AddrX");
        v.setCity("CityX");
        v.setState("StateX");
        v.setCountry("CountryX");
        Venue savedVenue = venueRepository.save(v);
        mockMvc.perform(get("/venues/" + savedVenue.getVenueId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("VenueX"));
    }

    @Test
    void testUpdateVenueIntegration() throws Exception {
        Venue tempV = new Venue();
        tempV.setName("VenueOld");
        tempV.setAddress("AddrOld");
        tempV.setCity("CityOld");
        tempV.setState("StateOld");
        tempV.setCountry("CountryOld");
        Venue v = venueRepository.save(tempV);
        CreateVenueDto dto = new CreateVenueDto();
        dto.setName("VenueNew");
        dto.setAddress("AddrNew");
        dto.setCity("CityNew");
        dto.setState("StateNew");
        dto.setPostalCode("54321");
        dto.setCountry("CountryNew");
        dto.setContactPhone("0987654321");
        dto.setContactEmail("venueNew@example.com");
        mockMvc.perform(put("/venues/" + v.getVenueId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("VenueNew"));
    }

    @Test
    void testDeleteVenueIntegration() throws Exception {
        Venue v = new Venue();
        v.setName("VenueDel");
        v.setAddress("AddrDel");
        v.setCity("CityDel");
        v.setState("StateDel");
        v.setCountry("CountryDel");
        v = venueRepository.save(v);
        mockMvc.perform(delete("/venues/" + v.getVenueId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("VenueDel"));
        assertThat(venueRepository.findById(v.getVenueId())).isEmpty();
    }

    @Test
    void testCreateTicketsForEventIntegration() throws Exception {
        venueRepository.save(testVenue);
        Event event = new Event();
        event.setName("TicketEvent");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setDescription("Event for tickets");
        event.setCreatorId(UUID.randomUUID());
        event.setVenue(testVenue);
        eventRepository.save(event);
        String payload = "{" +
                "\"ticketType\":\"VIP\"," +
                "\"price\":100.0," +
                "\"quantity\":1}";
        mockMvc.perform(post("/events/" + event.getId() + "/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Roles", "ADMIN")
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTicketsForEventIntegration() throws Exception {
        venueRepository.save(testVenue);
        Event event = new Event();
        event.setName("TicketEvent2");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setDescription("Event for tickets2");
        event.setCreatorId(UUID.randomUUID());
        event.setVenue(testVenue);
        eventRepository.save(event);
        Ticket ticket = new Ticket();
        ticket.setTicketType("VIP");
        ticket.setPrice(100.0);
        ticket.setTicketStatus(TicketStatus.AVAILABLE);
        ticket.setEvent(event);
        event.addTicket(ticket);
        ticketRepository.save(ticket);
        eventRepository.save(event);
        mockMvc.perform(get("/events/" + event.getId() + "/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(1)))
                .andExpect(jsonPath("$[0].ticketType").value("VIP"));
    }

    @Test
    void testGetTicketByIdIntegration() throws Exception {
        venueRepository.save(testVenue);
        Event event = new Event();
        event.setName("TicketEvent3");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setDescription("Event for tickets3");
        event.setCreatorId(UUID.randomUUID());
        event.setVenue(testVenue);
        eventRepository.save(event);
        Ticket ticket = new Ticket();
        ticket.setTicketType("REGULAR");
        ticket.setPrice(50.0);
        ticket.setTicketStatus(TicketStatus.AVAILABLE);
        event.addTicket(ticket);
        ticket.setEvent(event);
        ticketRepository.save(ticket);
        eventRepository.save(event);
        mockMvc.perform(get("/events/" + event.getId() + "/tickets/" + ticket.getTicketId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketType").value("REGULAR"));
    }

    @Test
    void testReserveEventTicketIntegration() throws Exception {
        venueRepository.save(testVenue);
        Event event = new Event();
        event.setName("TicketEvent4");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setDescription("Event for tickets4");
        event.setCreatorId(UUID.randomUUID());
        event.setVenue(testVenue);
        eventRepository.save(event);
        Ticket ticket = new Ticket();
        ticket.setTicketType("VIP");
        ticket.setPrice(100.0);
        ticket.setTicketStatus(TicketStatus.AVAILABLE);
        ticket.setEvent(event);
        event.addTicket(ticket);
        ticketRepository.save(ticket);
        eventRepository.save(event);
        mockMvc.perform(patch("/events/" + event.getId() + "/tickets/" + ticket.getTicketId())
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testIsTicketAvailableIntegration() throws Exception {
        venueRepository.save(testVenue);
        Event event = new Event();
        event.setName("TicketEvent5");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setDescription("Event for tickets5");
        event.setCreatorId(UUID.randomUUID());
        event.setVenue(testVenue);
        eventRepository.save(event);
        Ticket ticket = new Ticket();
        ticket.setTicketType("VIP");
        ticket.setPrice(100.0);
        ticket.setTicketStatus(TicketStatus.AVAILABLE);
        ticket.setEvent(event);
        event.addTicket(ticket);
        ticketRepository.save(ticket);
        eventRepository.save(event);
        mockMvc.perform(get("/events/" + event.getId() + "/tickets/" + ticket.getTicketId() + "/available"))
                .andExpect(status().isOk());
    }
}
