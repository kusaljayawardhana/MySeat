package com.kusal.myseat.repository;

import com.kusal.myseat.entity.Booking;
import com.kusal.myseat.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, Instant referenceTime);
}