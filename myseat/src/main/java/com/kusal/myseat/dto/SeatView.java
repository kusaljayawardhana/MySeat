package com.kusal.myseat.dto;

import com.kusal.myseat.entity.SeatStatus;

public record SeatView(
        Long id,
        Long sectionId,
        Integer rowNumber,
        Integer columnNumber,
        SeatStatus status
) {
}