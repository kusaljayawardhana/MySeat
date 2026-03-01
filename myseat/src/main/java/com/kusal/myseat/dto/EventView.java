package com.kusal.myseat.dto;

import java.time.LocalDateTime;

public record EventView(
        Long id,
        String name,
        String description,
        String imageUrl,
        LocalDateTime eventDate,
        Long venueId,
        String venueName,
        String venueAddress
) {
}