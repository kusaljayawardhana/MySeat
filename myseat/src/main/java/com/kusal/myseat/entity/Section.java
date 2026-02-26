package com.kusal.myseat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double price;

    private Integer totalRows;
    private Integer totalColumns;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}