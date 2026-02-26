package com.kusal.myseat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rowNumber;
    private Integer columnNumber;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;
}