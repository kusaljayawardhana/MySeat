package com.kusal.myseat.dto;

public record EventSectionView(
        Long sectionId,
        String sectionName,
        Double price,
        Integer totalRows,
        Integer totalColumns
) {
}
