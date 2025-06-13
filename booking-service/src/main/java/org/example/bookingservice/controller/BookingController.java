package org.example.bookingservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.dto.BookingRequestDto;
import org.example.bookingservice.dto.BookingResponseDto;
import org.example.bookingservice.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody BookingRequestDto bookingRequestDto,
                                                            @RequestHeader("X-User-Id") UUID userId) {
        BookingResponseDto created = bookingService.createBooking(bookingRequestDto, userId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable UUID id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDto> updateBooking(@PathVariable UUID id,
                                                            @RequestBody BookingRequestDto bookingRequestDto,
                                                            @RequestHeader("X-User-Id") UUID userId) {
        return bookingService.updateBooking(id, bookingRequestDto, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponseDto> deleteBooking(@PathVariable UUID id) {
        return bookingService.deleteBooking(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUserId(@PathVariable UUID userId,
                                                                        @RequestHeader("X-User-Id") UUID requestUserId,
                                                                        @RequestHeader("X-User-Roles") String userRole) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId, requestUserId, userRole));
    }

    @GetMapping("/current-user")
    public ResponseEntity<List<BookingResponseDto>> getCurrentUserBookings(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }
}