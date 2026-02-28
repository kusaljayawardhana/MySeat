package com.kusal.myseat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

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
    private Venue venue;

    @OneToMany(mappedBy = "section")
    private List<Seat> seats;

}