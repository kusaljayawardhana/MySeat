package com.kusal.myseat.dto;

import java.util.List;

public record BookingResponse(
        Long bookingId,
        Double totalAmount,
        List<Long> bookedSeatIds
) {
}