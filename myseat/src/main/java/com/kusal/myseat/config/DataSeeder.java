package com.kusal.myseat.config;

import com.kusal.myseat.entity.Event;
import com.kusal.myseat.entity.Role;
import com.kusal.myseat.entity.Seat;
import com.kusal.myseat.entity.SeatStatus;
import com.kusal.myseat.entity.Section;
import com.kusal.myseat.entity.User;
import com.kusal.myseat.entity.Venue;
import com.kusal.myseat.repository.EventRepository;
import com.kusal.myseat.repository.SeatRepository;
import com.kusal.myseat.repository.SectionRepository;
import com.kusal.myseat.repository.UserRepository;
import com.kusal.myseat.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        Venue mainVenue = seedVenue();
        seedEvents(mainVenue);
        seedSectionsAndSeats(mainVenue);
    }

    private void seedUsers() {
        if (userRepository.findByEmail("admin@myseat.com").isEmpty()) {
            userRepository.save(User.builder()
                    .name("System Admin")
                    .email("admin@myseat.com")
                    .password("admin123")
                    .role(Role.ADMIN)
                    .build());
        }

        if (userRepository.findByEmail("user@myseat.com").isEmpty()) {
            userRepository.save(User.builder()
                    .name("Demo User")
                    .email("user@myseat.com")
                    .password("user123")
                    .role(Role.USER)
                    .build());
        }
    }

    private Venue seedVenue() {
        return venueRepository.findByName("MySeat Main Auditorium")
                .orElseGet(() -> {
                    Venue venue = new Venue();
                    venue.setName("MySeat Main Auditorium");
                    venue.setAddress("Colombo 07");
                    return venueRepository.save(venue);
                });
    }

    private void seedEvents(Venue venue) {
        boolean hasConcert = eventRepository.findAll().stream()
                .anyMatch(event -> "Friday Live Concert".equalsIgnoreCase(event.getName()));
        if (!hasConcert) {
            eventRepository.save(Event.builder()
                    .name("Friday Live Concert")
                    .description("An energetic live music night featuring popular local artists and a full-stage production.")
                    .eventDate(LocalDateTime.now().plusDays(7))
                    .venue(venue)
                    .build());
        }

        boolean hasDrama = eventRepository.findAll().stream()
                .anyMatch(event -> "Weekend Drama Show".equalsIgnoreCase(event.getName()));
        if (!hasDrama) {
            eventRepository.save(Event.builder()
                    .name("Weekend Drama Show")
                    .description("A family-friendly stage drama with two acts, intermission, and reserved seating.")
                    .eventDate(LocalDateTime.now().plusDays(14))
                    .venue(venue)
                    .build());
        }
    }

    private void seedSectionsAndSeats(Venue venue) {
        Section normalSection = findOrCreateSection(venue, "NORMAL", 2500.0, 8, 10);
        Section balconySection = findOrCreateSection(venue, "BALCONY", 4000.0, 5, 8);

        if (seatRepository.count() == 0) {
            List<Seat> seats = new ArrayList<>();
            seats.addAll(generateSeats(normalSection, normalSection.getTotalRows(), normalSection.getTotalColumns()));
            seats.addAll(generateSeats(balconySection, balconySection.getTotalRows(), balconySection.getTotalColumns()));
            seatRepository.saveAll(seats);
        }
    }

    private Section findOrCreateSection(Venue venue, String name, Double price, int rows, int cols) {
        return sectionRepository.findAll().stream()
                .filter(section -> section.getVenue().getId().equals(venue.getId()))
                .filter(section -> section.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> sectionRepository.save(Section.builder()
                        .name(name)
                        .price(price)
                        .totalRows(rows)
                        .totalColumns(cols)
                        .venue(venue)
                        .build()));
    }

    private List<Seat> generateSeats(Section section, int rows, int cols) {
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                seats.add(Seat.builder()
                        .rowNumber(row)
                        .columnNumber(col)
                        .status(SeatStatus.AVAILABLE)
                        .section(section)
                        .build());
            }
        }
        return seats;
    }
}
