package com.kusal.myseat.service;

import com.kusal.myseat.dto.CreateEventRequest;
import com.kusal.myseat.dto.CreateSectionRequest;
import com.kusal.myseat.dto.CreateUserRequest;
import com.kusal.myseat.dto.CreateVenueRequest;
import com.kusal.myseat.dto.EventView;
import com.kusal.myseat.dto.SeatView;
import com.kusal.myseat.entity.*;
import com.kusal.myseat.repository.EventRepository;
import com.kusal.myseat.repository.SectionRepository;
import com.kusal.myseat.repository.SeatRepository;
import com.kusal.myseat.repository.UserRepository;
import com.kusal.myseat.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final EventRepository eventRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final BookingService bookingService;

    public Event createEvent(CreateEventRequest request) {
        Venue venue = resolveVenueForEvent(request);
        LocalDateTime eventDate = parseEventDate(request.eventDate());

        Event event = Event.builder()
                .name(request.name())
            .description(request.description())
                .venue(venue)
                .eventDate(eventDate)
                .build();
        return eventRepository.save(event);
    }

    public List<EventView> getEvents() {
        return eventRepository.findAllByOrderByEventDateAsc()
                .stream()
                .map(this::toEventView)
                .toList();
    }

    public EventView getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return toEventView(event);
    }

    public Venue createVenue(CreateVenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name());
        venue.setAddress(request.address());
        return venueRepository.save(venue);
    }

    public List<Venue> getVenues() {
        return venueRepository.findAll();
    }

    @Transactional
    public Section createSection(CreateSectionRequest request) {
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        Section section = Section.builder()
                .name(request.name())
                .price(request.price())
                .totalRows(request.totalRows())
                .totalColumns(request.totalColumns())
                .venue(venue)
                .build();

        Section savedSection = sectionRepository.save(section);
        
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= request.totalRows(); row++) {
            for (int col = 1; col <= request.totalColumns(); col++) {
                seats.add(Seat.builder()
                        .rowNumber(row)
                        .columnNumber(col)
                        .status(SeatStatus.AVAILABLE)
                        .section(savedSection)
                        .build());
            }
        }
        seatRepository.saveAll(seats);

        return savedSection;
    }

    public User createUser(CreateUserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .role(request.role())
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public List<SeatView> getSeatsForEvent(Long eventId) {
        bookingService.expireStaleReservations();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        ensureSeatsForVenue(event.getVenue().getId());

        return seatRepository.findBySectionVenueId(event.getVenue().getId())
                .stream()
            .filter(seat -> seat.getSection() != null)
            .map(seat -> new SeatView(seat.getId(), seat.getSection().getId(), seat.getRowNumber(), seat.getColumnNumber(), seat.getStatus()))
                .toList();
    }

    private Venue resolveVenueForEvent(CreateEventRequest request) {
        if (request.venueId() != null && request.venue() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide either venueId or venue details, not both");
        }

        if (request.venueId() != null) {
            return venueRepository.findById(request.venueId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));
        }

        if (request.venue() != null) {
            Venue newVenue = new Venue();
            newVenue.setName(request.venue().name());
            newVenue.setAddress(request.venue().address());
            return venueRepository.save(newVenue);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either venueId or venue details must be provided");
    }

    private LocalDateTime parseEventDate(String eventDate) {
        try {
            return LocalDateTime.parse(eventDate);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventDate must be in ISO-8601 format, e.g. 2026-02-28T19:30:00");
        }
    }

    private EventView toEventView(Event event) {
        return new EventView(
                event.getId(),
                event.getName(),
            event.getDescription(),
                event.getEventDate(),
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getVenue().getAddress()
        );
    }

    private void ensureSeatsForVenue(Long venueId) {
        List<Section> sections = sectionRepository.findByVenueId(venueId);
        if (sections.isEmpty()) {
            return;
        }

        List<Seat> seatsToCreate = new ArrayList<>();
        for (Section section : sections) {
            long existingSeatCount = seatRepository.countBySectionId(section.getId());
            if (existingSeatCount > 0) {
                continue;
            }

            Integer totalRows = section.getTotalRows();
            Integer totalColumns = section.getTotalColumns();
            if (totalRows == null || totalColumns == null || totalRows < 1 || totalColumns < 1) {
                continue;
            }

            for (int row = 1; row <= totalRows; row++) {
                for (int col = 1; col <= totalColumns; col++) {
                    seatsToCreate.add(Seat.builder()
                            .rowNumber(row)
                            .columnNumber(col)
                            .status(SeatStatus.AVAILABLE)
                            .section(section)
                            .build());
                }
            }
        }

        if (!seatsToCreate.isEmpty()) {
            seatRepository.saveAll(seatsToCreate);
        }
    }


}

