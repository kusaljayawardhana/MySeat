package com.kusal.myseat.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
        @NotBlank String name,
        @NotBlank String venue,
        @NotBlank String eventDate
) {
}