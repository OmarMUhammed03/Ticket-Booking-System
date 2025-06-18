package org.example.bookingservice.mapper;

import org.example.bookingservice.dto.BookingRequestDto;
import org.example.bookingservice.dto.BookingResponseDto;
import org.example.bookingservice.model.Booking;
import org.example.bookingservice.model.BookingStatus;

import java.util.UUID;

public class BookingMapper {
    public static Booking toBooking(final BookingRequestDto dto, final UUID userId) {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEventId(dto.getEventId());
        booking.setTicketId(dto.getTicketId());
        booking.setBookingStatus(dto.getBookingStatus() != null ? BookingStatus.valueOf(dto.getBookingStatus()) : null);
        booking.setBookingDate(dto.getBookingDate());
        booking.setBookingDetail(dto.getBookingDetail());
        return booking;
    }

    public static BookingResponseDto toBookingResponseDto(final Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUserId());
        dto.setEventId(booking.getEventId());
        dto.setTicketId(booking.getTicketId());
        dto.setBookingStatus(booking.getBookingStatus() != null ? booking.getBookingStatus().name() : null);
        dto.setBookingDate(booking.getBookingDate());
        dto.setBookingDetail(booking.getBookingDetail());
        return dto;
    }
}

