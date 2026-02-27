package com.kusal.myseat.controller;

import com.kusal.myseat.dto.BookingResponse;
import com.kusal.myseat.dto.CreateBookingRequest;
import com.kusal.myseat.dto.ConfirmBookingRequest;
import com.kusal.myseat.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }
    @PostMapping("/confirm")
    public BookingResponse confirmBooking(@Valid @RequestBody ConfirmBookingRequest request) {
        return bookingService.confirmBooking(request);
    }
}