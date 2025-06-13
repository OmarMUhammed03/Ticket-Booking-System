package org.example.bookingservice.service;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.model.BookingStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.example.bookingservice.dto.BookingRequestDto;
import org.example.bookingservice.dto.BookingResponseDto;
import org.example.bookingservice.mapper.BookingMapper;
import org.example.bookingservice.model.Booking;
import org.example.bookingservice.repository.BookingRepository;
import org.example.commonlibrary.InvalidActionException;
import org.example.commonlibrary.ValidationException;
import org.example.commonlibrary.kafka.MessageProducer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.bookingservice.model.BookingStatus.PENDING;

@RequiredArgsConstructor
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final MessageProducer messageProducer;
    private static final String EVENT_SERVICE_URL = "http://localhost:8082/api/events/";
    private static final int TICKET_EXPIRATION_DURATION_MINUTES = 10;
    private static final HashMap<UUID, LocalDateTime> ticketLastReserved = new HashMap<>();

    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, UUID userId) {
        if (bookingRequestDto.getEventId() == null || bookingRequestDto.getTicketId() == null) {
            throw new ValidationException("Event ID and Ticket ID must not be null");
        }
        RestTemplate restTemplate = new RestTemplate();
        String ticketAvailableUrl = EVENT_SERVICE_URL + bookingRequestDto.getEventId() + "/tickets/" + bookingRequestDto.getTicketId() + "/available";
        ResponseEntity<Boolean> response = restTemplate.getForEntity(ticketAvailableUrl, Boolean.class);
        if (Boolean.FALSE.equals(response.getBody())) {
            throw new ValidationException("Ticket is not available");
        }
        if (ticketLastReserved.containsKey(bookingRequestDto.getTicketId())) {
            LocalDateTime lastReserved = ticketLastReserved.get(bookingRequestDto.getTicketId());
            if (lastReserved.isAfter(LocalDateTime.now().minusMinutes(TICKET_EXPIRATION_DURATION_MINUTES))) {
                throw new ValidationException("Ticket is already reserved, please try again later");
            }
        }
        bookingRequestDto.setBookingDate(LocalDateTime.now());
        bookingRequestDto.setBookingStatus(String.valueOf(PENDING));
        Booking booking = BookingMapper.toBooking(bookingRequestDto, userId);
        Booking created = bookingRepository.save(booking);
        messageProducer.sendMessage("reserve-ticket", "ticket-available", new HashMap<>(
                Map.of("ticketId", bookingRequestDto.getTicketId(), "eventId", bookingRequestDto.getEventId()
                        , "userId", userId, "bookingId", created.getId())
        ));
        ticketLastReserved.put(bookingRequestDto.getTicketId(), LocalDateTime.now());
        return BookingMapper.toBookingResponseDto(created);
    }

    public Optional<BookingResponseDto> getBookingById(UUID id) {
        return bookingRepository.findById(id).map(BookingMapper::toBookingResponseDto);
    }

    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll().stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    public Optional<BookingResponseDto> updateBooking(UUID id, BookingRequestDto bookingRequestDto, UUID userId) {
        return bookingRepository.findById(id).map(existing -> {
            Booking updatedBooking = BookingMapper.toBooking(bookingRequestDto, userId);
            if (bookingRequestDto.getBookingStatus() != null)
                existing.setBookingStatus(updatedBooking.getBookingStatus());
            if (bookingRequestDto.getBookingDate() != null) existing.setBookingDate(updatedBooking.getBookingDate());
            if (bookingRequestDto.getBookingDetail() != null)
                existing.setBookingDetail(updatedBooking.getBookingDetail());
            Booking saved = bookingRepository.save(existing);
            return BookingMapper.toBookingResponseDto(saved);
        });
    }

    public Optional<BookingResponseDto> updateBookingStatus(UUID id, String status) {
        return bookingRepository.findById(id).map(existing -> {
            existing.setBookingStatus(BookingStatus.valueOf(status));
            Booking saved = bookingRepository.save(existing);
            return BookingMapper.toBookingResponseDto(saved);
        });
    }

    public Optional<BookingResponseDto> deleteBooking(UUID id) {
        return bookingRepository.findById(id).map(existing -> {
            bookingRepository.deleteById(id);
            return BookingMapper.toBookingResponseDto(existing);
        });
    }

    public List<BookingResponseDto> getBookingsByUserId(UUID userId, UUID requestUserId, String userRole) {
        if (userId == requestUserId || userRole.equals("ADMIN")) {
            return getBookingsByUserId(userId);
        }
        throw new InvalidActionException("Unauthorized access to bookings of another user");
    }

    public List<BookingResponseDto> getBookingsByUserId(UUID userId) {
        return bookingRepository.findByUserId(userId).stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }
}
