package com.kusal.myseat.dto;

import com.kusal.myseat.entity.SeatStatus;

public record SeatView(
        Long id,
        Integer rowNumber,
        Integer columnNumber,
        SeatStatus status
) {
}