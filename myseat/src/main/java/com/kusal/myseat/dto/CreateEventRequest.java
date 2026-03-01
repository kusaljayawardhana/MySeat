package com.kusal.myseat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
        @NotBlank String name,
        String description,
        String imageUrl,
        @NotBlank String eventDate,
        Long venueId,
        @Valid CreateVenueRequest venue
) {
}