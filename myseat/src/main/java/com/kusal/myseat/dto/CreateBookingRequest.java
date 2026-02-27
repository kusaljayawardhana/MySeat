package com.kusal.myseat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBookingRequest(
        @NotNull Long userId,
        @NotNull Long sectionId,
        @NotEmpty List<Long> seatIds
) {
}