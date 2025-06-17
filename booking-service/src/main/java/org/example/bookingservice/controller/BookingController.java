package org.example.bookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.dto.BookingRequestDto;
import org.example.bookingservice.dto.BookingResponseDto;
import org.example.bookingservice.service.BookingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Booking Controller", description = "Endpoints for managing bookings")
@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Operation(summary = "Create a new booking", description = "Creates a new booking record based on the provided request data.")
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody BookingRequestDto bookingRequestDto,
                                                            @RequestHeader("X-User-Id") UUID userId,
                                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        BookingResponseDto created = bookingService.createBooking(bookingRequestDto, userId, authorization);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Get booking by ID", description = "Retrieves a booking record by its unique identifier.")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable UUID id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all bookings", description = "Fetches all booking records.")
    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @Operation(summary = "Update a booking", description = "Updates an existing booking record identified by its unique ID.")
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDto> updateBooking(@PathVariable UUID id,
                                                            @RequestBody BookingRequestDto bookingRequestDto,
                                                            @RequestHeader("X-User-Id") UUID userId) {
        return bookingService.updateBooking(id, bookingRequestDto, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a booking", description = "Deletes a booking record by its unique identifier.")
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