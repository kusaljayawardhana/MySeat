package com.kusal.myseat.controller;

import com.kusal.myseat.dto.CreateEventRequest;
import com.kusal.myseat.dto.CreateSectionRequest;
import com.kusal.myseat.dto.CreateUserRequest;
import com.kusal.myseat.dto.SeatView;
import com.kusal.myseat.entity.Event;
import com.kusal.myseat.entity.Section;
import com.kusal.myseat.entity.User;
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