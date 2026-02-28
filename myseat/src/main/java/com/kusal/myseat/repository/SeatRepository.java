package com.kusal.myseat.repository;

import com.kusal.myseat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySectionVenueId(Long venueId);
}