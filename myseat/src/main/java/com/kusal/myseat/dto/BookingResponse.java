package com.kusal.myseat.dto;

import java.util.List;
import com.kusal.myseat.entity.BookingStatus;

import java.time.Instant;

public record BookingResponse(
        Long bookingId,
        Double totalAmount,
        BookingStatus status,
        Instant expiresAt,
        List<Long> seatIds,
        List<Long> bookedSeatIds
) {
}