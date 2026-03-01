package com.kusal.myseat.controller;

import com.kusal.myseat.dto.CreateEventRequest;
import com.kusal.myseat.dto.CreateSectionRequest;
import com.kusal.myseat.dto.CreateUserRequest;
import com.kusal.myseat.dto.CreateVenueRequest;
import com.kusal.myseat.dto.EventView;
import com.kusal.myseat.dto.EventSectionView;
import com.kusal.myseat.dto.SeatView;
import com.kusal.myseat.entity.Event;
import com.kusal.myseat.entity.Section;
import com.kusal.myseat.entity.User;
import com.kusal.myseat.entity.Venue;
import com.kusal.myseat.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @PostMapping("/events")
    public Event createEvent(@Valid @RequestBody CreateEventRequest request) {
        return catalogService.createEvent(request);
    }

    @GetMapping("/events")
    public List<EventView> getEvents() {
        return catalogService.getEvents();
    }

    @GetMapping("/events/{eventId}")
    public EventView getEventById(@PathVariable("eventId") Long eventId) {
        return catalogService.getEventById(eventId);
    }


    @GetMapping("/venues/{venueId}/sections")
    public List<EventSectionView> getSectionsForVenue(@PathVariable Long venueId) {
        return catalogService.getSectionsForVenue(venueId);
    }

    @PostMapping("/venues")
    public Venue createVenue(@Valid @RequestBody CreateVenueRequest request) {
        return catalogService.createVenue(request);
    }

    @GetMapping("/venues")
    public List<Venue> getVenues() {
        return catalogService.getVenues();
    }

    @PostMapping("/sections")
    public Section createSection(@Valid @RequestBody CreateSectionRequest request) {
        return catalogService.createSection(request);
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        return catalogService.createUser(request);
    }

    @GetMapping("/events/{eventId}/seats")
    public List<SeatView> getSeatsForEvent(@PathVariable Long eventId) {
        return catalogService.getSeatsForEvent(eventId);
    }
}