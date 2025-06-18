package org.example.bookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.dto.BookingRequestDto;
import org.example.bookingservice.dto.BookingResponseDto;
import org.example.bookingservice.service.BookingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

@Tag(name = "Booking Controller", description = "Endpoints for managing bookings")
@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Operation(summary = "Create a new booking",
            description = "Creates a new booking record based on the provided request data.")
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody final BookingRequestDto bookingRequestDto,
                                                            @RequestHeader("X-User-Id") final UUID userId,
                                                            @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                final String authorization) {
        BookingResponseDto created = bookingService.createBooking(bookingRequestDto, userId, authorization);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Get booking by ID", description = "Retrieves a booking record by its unique identifier.")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable final UUID id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all bookings", description = "Fetches all booking records.")
    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @Operation(summary = "Update a booking",
            description = "Updates an existing booking record identified by its unique ID.")
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDto> updateBooking(@PathVariable final UUID id,
                                                            @RequestBody final BookingRequestDto bookingRequestDto,
                                                            @RequestHeader("X-User-Id") final UUID userId) {
        return bookingService.updateBooking(id, bookingRequestDto, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a booking", description = "Deletes a booking record by its unique identifier.")
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponseDto> deleteBooking(@PathVariable final UUID id) {
        return bookingService.deleteBooking(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUserId(@PathVariable final UUID userId,
                                                                        @RequestHeader("X-User-Id")
                                                                        final UUID requestUserId,
                                                                        @RequestHeader("X-User-Roles")
                                                                            final String userRole) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId, requestUserId, userRole));
    }

    @GetMapping("/current-user")
    public ResponseEntity<List<BookingResponseDto>> getCurrentUserBookings(@RequestHeader("X-User-Id")
                                                                               final UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }
}
