package com.kusal.myseat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSectionRequest(
        @NotBlank String name,
        @NotNull @Min(0) Double price,
        @NotNull @Min(1) Integer totalRows,
        @NotNull @Min(1) Integer totalColumns,
        @NotNull Long eventId
) {
}