package com.kusal.myseat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Instant reservedAt;

    private Instant expiresAt;

    private Instant confirmedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}