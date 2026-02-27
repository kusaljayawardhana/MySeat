package com.kusal.myseat.service;

import com.kusal.myseat.dto.BookingResponse;
import com.kusal.myseat.dto.CreateBookingRequest;
import com.kusal.myseat.entity.*;
import com.kusal.myseat.repository.BookingRepository;
import com.kusal.myseat.repository.BookingSeatRepository;
import com.kusal.myseat.repository.SectionRepository;
import com.kusal.myseat.repository.SeatRepository;
import com.kusal.myseat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Section section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        List<Seat> seats = seatRepository.findAllByIdInForUpdate(request.seatIds());
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One or more seats are already booked");
        }

        seats.forEach(seat -> seat.setStatus(SeatStatus.BOOKED));
        seatRepository.saveAll(seats);

        double totalAmount = section.getPrice() * seats.size();

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .totalAmount(totalAmount)
                .build());

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> BookingSeat.builder()
                        .booking(booking)
                        .seat(seat)
                        .build())
                .toList();
        bookingSeatRepository.saveAll(bookingSeats);

        return new BookingResponse(
                booking.getId(),
                booking.getTotalAmount(),
                seats.stream().map(Seat::getId).toList()
        );
    }
}