package org.example.bookingservice.service;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.model.BookingStatus;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.bookingservice.model.BookingStatus.PENDING;

@RequiredArgsConstructor
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final MessageProducer messageProducer;
    private static final String GATEWAY_URL = "http://localhost:8088/api/events/";
    private final CacheManager cacheManager;
    private final RestTemplate restTemplate;

    public BookingResponseDto createBooking(final BookingRequestDto bookingRequestDto, final UUID userId,
                                            final String authHeader) {
        if (bookingRequestDto.getEventId() == null || bookingRequestDto.getTicketId() == null) {
            throw new ValidationException("Event ID and Ticket ID must not be null");
        }
        String ticketAvailableUrl = GATEWAY_URL + bookingRequestDto.getEventId() + "/tickets/"
                + bookingRequestDto.getTicketId() + "/available";
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(ticketAvailableUrl,
                HttpMethod.GET,
                requestEntity,
                Boolean.class);
        if (Boolean.FALSE.equals(response.getBody())) {
            throw new ValidationException("Ticket is not available");
        }
        Cache ticketReservationCache = cacheManager.getCache("ticketReservations");
        if (ticketReservationCache != null && ticketReservationCache.get(bookingRequestDto.getTicketId()) != null) {
            throw new ValidationException("Ticket is already reserved, please try again later");
        }
        bookingRequestDto.setBookingDate(LocalDateTime.now());
        bookingRequestDto.setBookingStatus(String.valueOf(PENDING));
        Booking booking = BookingMapper.toBooking(bookingRequestDto, userId);
        Booking created = bookingRepository.save(booking);
        messageProducer.sendMessage("reserve-ticket", "ticket-available", new HashMap<>(
                Map.of("ticketId", bookingRequestDto.getTicketId(), "eventId", bookingRequestDto.getEventId(),
                        "userId", userId, "bookingId", created.getId())
        ));
        ticketReservationCache.put(bookingRequestDto.getTicketId(), true);
        return BookingMapper.toBookingResponseDto(created);
    }

    public Optional<BookingResponseDto> getBookingById(final UUID id) {
        return bookingRepository.findById(id).map(BookingMapper::toBookingResponseDto);
    }

    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll().stream().map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<BookingResponseDto> updateBooking(final UUID id, final BookingRequestDto bookingRequestDto,
                                                      final UUID userId) {
        return bookingRepository.findById(id).map(existing -> {
            Booking updatedBooking = BookingMapper.toBooking(bookingRequestDto, userId);
            if (bookingRequestDto.getBookingStatus() != null) {
                existing.setBookingStatus(updatedBooking.getBookingStatus());
            }
            if (bookingRequestDto.getBookingDate() != null) {
                existing.setBookingDate(updatedBooking.getBookingDate());
            }
            if (bookingRequestDto.getBookingDetail() != null) {
                existing.setBookingDetail(updatedBooking.getBookingDetail());
            }
            Booking saved = bookingRepository.save(existing);
            return BookingMapper.toBookingResponseDto(saved);
        });
    }

    public Optional<BookingResponseDto> updateBookingStatus(final UUID id, final String status) {
        return bookingRepository.findById(id).map(existing -> {
            existing.setBookingStatus(BookingStatus.valueOf(status));
            Booking saved = bookingRepository.save(existing);
            return BookingMapper.toBookingResponseDto(saved);
        });
    }

    public Optional<BookingResponseDto> deleteBooking(final UUID id) {
        return bookingRepository.findById(id).map(existing -> {
            bookingRepository.deleteById(id);
            return BookingMapper.toBookingResponseDto(existing);
        });
    }

    public List<BookingResponseDto> getBookingsByUserId(final UUID userId, final UUID requestUserId,
                                                        final String userRole) {
        if (userId.equals(requestUserId) || userRole.equals("ADMIN")) {
            return getBookingsByUserId(userId);
        }
        throw new InvalidActionException("Unauthorized access to bookings of another user");
    }

    public List<BookingResponseDto> getBookingsByUserId(final UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }
}
