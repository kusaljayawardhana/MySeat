package com.kusal.myseat.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmBookingRequest(
        @NotNull Long bookingId
) {
}