package com.kusal.myseat.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateVenueRequest(
        @NotBlank String name,
        @NotBlank String address
) {
}
