package com.kusal.myseat.service;

import com.kusal.myseat.dto.CreateEventRequest;
import com.kusal.myseat.dto.CreateSectionRequest;
import com.kusal.myseat.dto.CreateUserRequest;
import com.kusal.myseat.dto.SeatView;
import com.kusal.myseat.entity.*;
import com.kusal.myseat.repository.EventRepository;
import com.kusal.myseat.repository.SectionRepository;
import com.kusal.myseat.repository.SeatRepository;
import com.kusal.myseat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final EventRepository eventRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public Event createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .name(request.name())
                .venue(request.venue())
                .eventDate(request.eventDate())
                .build();
        return eventRepository.save(event);
    }

    @Transactional
    public Section createSection(CreateSectionRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        Section section = Section.builder()
                .name(request.name())
                .price(request.price())
                .totalRows(request.totalRows())
                .totalColumns(request.totalColumns())
                .event(event)
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

    public List<SeatView> getSeatsForEvent(Long eventId) {
        bookingService.expireStaleReservations();

        return seatRepository.findBySectionEventId(eventId)
                .stream()
                .map(seat -> new SeatView(seat.getId(), seat.getRowNumber(), seat.getColumnNumber(), seat.getStatus()))
                .toList();
    }


}

