package org.example.bookingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookingservice.dto.BookingRequestDto;
import org.example.bookingservice.model.Booking;
import org.example.bookingservice.model.BookingStatus;
import org.example.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private UUID userId;
    private UUID eventId;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    void reset() {
        bookingRepository.deleteAll();
    }

    @Test
    void testCreateBookingIntegration() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo("http://localhost:8088/api/events/" + eventId + "/tickets/" +
                                ticketId + "/available"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK) // Respond with 200 OK
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("true"));
        BookingRequestDto dto = new BookingRequestDto();
        dto.setEventId(eventId);
        dto.setTicketId(ticketId);
        dto.setBookingStatus("CONFIRMED");
        dto.setBookingDate(LocalDateTime.now());
        dto.setBookingDetail("Test booking");
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId.toString()));
        mockServer.verify();
    }

    @Test
    void testGetBookingByIdIntegration() throws Exception {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEventId(eventId);
        booking.setTicketId(ticketId);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingDetail("Detail");
        bookingRepository.save(booking);
        mockMvc.perform(get("/bookings/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()));
    }

    @Test
    void testGetAllBookingsIntegration() throws Exception {
        Booking booking1 = new Booking();
        booking1.setUserId(userId);
        booking1.setEventId(eventId);
        booking1.setTicketId(ticketId);
        booking1.setBookingStatus(BookingStatus.CONFIRMED);
        booking1.setBookingDate(LocalDateTime.now());
        booking1.setBookingDetail("Detail1");
        bookingRepository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setUserId(UUID.randomUUID());
        booking2.setEventId(eventId);
        booking2.setTicketId(ticketId);
        booking2.setBookingStatus(BookingStatus.CANCELLED);
        booking2.setBookingDate(LocalDateTime.now());
        booking2.setBookingDetail("Detail2");
        bookingRepository.save(booking2);
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    void testUpdateBookingIntegration() throws Exception {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEventId(eventId);
        booking.setTicketId(ticketId);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingDetail("Old detail");
        bookingRepository.save(booking);
        BookingRequestDto dto = new BookingRequestDto();
        dto.setEventId(eventId);
        dto.setTicketId(ticketId);
        dto.setBookingStatus("CANCELLED");
        dto.setBookingDate(LocalDateTime.now());
        dto.setBookingDetail("Updated detail");
        mockMvc.perform(put("/bookings/" + booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"));
    }

    @Test
    void testDeleteBookingIntegration() throws Exception {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEventId(eventId);
        booking.setTicketId(ticketId);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingDetail("To delete");
        bookingRepository.save(booking);
        mockMvc.perform(delete("/bookings/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()));
        assert (bookingRepository.findById(booking.getId())).isEmpty();
    }

    @Test
    void testGetBookingsByUserIdIntegration() throws Exception {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEventId(eventId);
        booking.setTicketId(ticketId);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingDetail("By user");
        bookingRepository.save(booking);
        mockMvc.perform(get("/bookings/user/" + userId)
                        .header("X-User-Id", userId)
                        .header("X-User-Roles", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    void testGetCurrentUserBookingsIntegration() throws Exception {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEventId(eventId);
        booking.setTicketId(ticketId);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingDetail("Current user");
        bookingRepository.save(booking);
        mockMvc.perform(get("/bookings/current-user")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }
}

