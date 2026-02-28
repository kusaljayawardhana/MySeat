package com.kusal.myseat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBookingRequest(
        Long userId,
        @NotNull Long eventId,
        @NotNull Long venueId,
        @NotNull Long sectionId,
        @NotEmpty List<Long> seatIds,
        @NotBlank String payerName,
        @NotBlank @Email String payerEmail,
        @NotBlank String paymentMethod,
        @NotBlank String paymentReference
) {
}