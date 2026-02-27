package com.kusal.myseat.service;

import com.kusal.myseat.dto.BookingResponse;
import com.kusal.myseat.dto.ConfirmBookingRequest;
import com.kusal.myseat.dto.CreateBookingRequest;
import com.kusal.myseat.entity.*;
import com.kusal.myseat.repository.BookingRepository;
import com.kusal.myseat.repository.BookingSeatRepository;
import com.kusal.myseat.repository.SectionRepository;
import com.kusal.myseat.repository.SeatRepository;
import com.kusal.myseat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @Value("${booking.reservation-timeout-seconds:300}")
    private long reservationTimeoutSeconds;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        expireStaleReservations();

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Section section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more seats do not exist");
        }

        boolean hasWrongSectionSeat = seats.stream()
                .anyMatch(seat -> !seat.getSection().getId().equals(section.getId()));
        if (hasWrongSectionSeat) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat does not belong to selected section");
        }

        boolean anyUnavailable = seats.stream()
                .anyMatch(seat -> seat.getStatus() != SeatStatus.AVAILABLE);
        if (anyUnavailable) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One or more seats are not available");
        }

        seats.forEach(seat -> seat.setStatus(SeatStatus.RESERVED));
        seatRepository.saveAll(seats);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofSeconds(reservationTimeoutSeconds));
        double totalAmount = section.getPrice() * seats.size();

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(BookingStatus.RESERVED)
                .reservedAt(now)
                .expiresAt(expiresAt)
                .build());

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> BookingSeat.builder()
                        .booking(booking)
                        .seat(seat)
                        .build())
                .toList();
        bookingSeatRepository.saveAll(bookingSeats);

        return toResponse(booking, seats);
    }

    @Transactional
    public BookingResponse confirmBooking(ConfirmBookingRequest request) {
        expireStaleReservations();

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is not in reserved state");
        }

        Instant now = Instant.now();
        if (booking.getExpiresAt() != null && !booking.getExpiresAt().isAfter(now)) {
            expireBooking(booking);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation expired");
        }

        List<Seat> seats = bookingSeatRepository.findByBookingId(booking.getId())
                .stream()
                .map(BookingSeat::getSeat)
                .toList();

        boolean anyNotReserved = seats.stream().anyMatch(seat -> seat.getStatus() != SeatStatus.RESERVED);
        if (anyNotReserved) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reserved seats are no longer valid");
        }

        seats.forEach(seat -> seat.setStatus(SeatStatus.BOOKED));
        seatRepository.saveAll(seats);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(now);
        bookingRepository.save(booking);

        return toResponse(booking, seats);
    }

    @Transactional
    public void expireStaleReservations() {
        Instant now = Instant.now();
        List<Booking> staleBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.RESERVED, now);
        staleBookings.forEach(this::expireBooking);
    }

    private void expireBooking(Booking booking) {
        List<Seat> seats = bookingSeatRepository.findByBookingId(booking.getId())
                .stream()
                .map(BookingSeat::getSeat)
                .filter(seat -> seat.getStatus() == SeatStatus.RESERVED)
                .toList();

        seats.forEach(seat -> seat.setStatus(SeatStatus.AVAILABLE));
        seatRepository.saveAll(seats);

        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
    }

    private BookingResponse toResponse(Booking booking, List<Seat> seats) {
        return new BookingResponse(
                booking.getId(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getExpiresAt(),
                seats.stream().map(Seat::getId).toList(), null
        );
    }
}
