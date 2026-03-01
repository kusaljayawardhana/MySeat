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

    private static final String DEFAULT_EVENT_DESCRIPTION = "Event details will be updated soon.";
    private static final String DEFAULT_EVENT_IMAGE = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1200&q=80";
    private static final String FRIDAY_CONCERT_IMAGE = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=1200&q=80";
    private static final String WEEKEND_DRAMA_IMAGE = "https://images.unsplash.com/photo-1503095396549-807759245b35?auto=format&fit=crop&w=1200&q=80";

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
        List<Event> allEvents = eventRepository.findAll();

        upsertEvent(
            allEvents,
            "Friday Live Concert",
            "An energetic live music night featuring popular local artists and a full-stage production.",
            FRIDAY_CONCERT_IMAGE,
            LocalDateTime.now().plusDays(7),
            venue
        );

        upsertEvent(
            allEvents,
            "Weekend Drama Show",
            "A family-friendly stage drama with two acts, intermission, and reserved seating.",
            WEEKEND_DRAMA_IMAGE,
            LocalDateTime.now().plusDays(14),
            venue
        );

        repairEventsWithNulls(venue);
    }

    private void seedSectionsAndSeats(Venue venue) {
        Section normalSection = findOrCreateSection(venue, "NORMAL", 2500.0, 8, 10);
        Section balconySection = findOrCreateSection(venue, "BALCONY", 4000.0, 5, 8);

        seedSeatsForSectionIfMissing(normalSection);
        seedSeatsForSectionIfMissing(balconySection);
    }

    private void upsertEvent(
            List<Event> allEvents,
            String name,
            String description,
            String imageUrl,
            LocalDateTime eventDate,
            Venue venue
    ) {
        Event existing = allEvents.stream()
                .filter(event -> name.equalsIgnoreCase(event.getName()))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            eventRepository.save(Event.builder()
                    .name(name)
                    .description(description)
                    .imageUrl(imageUrl)
                    .eventDate(eventDate)
                    .venue(venue)
                    .build());
            return;
        }

        boolean changed = false;
        if (isBlank(existing.getDescription())) {
            existing.setDescription(description);
            changed = true;
        }
        if (isBlank(existing.getImageUrl())) {
            existing.setImageUrl(imageUrl);
            changed = true;
        }
        if (existing.getVenue() == null) {
            existing.setVenue(venue);
            changed = true;
        }
        if (existing.getEventDate() == null) {
            existing.setEventDate(eventDate);
            changed = true;
        }

        if (changed) {
            eventRepository.save(existing);
        }
    }

    private void repairEventsWithNulls(Venue defaultVenue) {
        List<Event> allEvents = eventRepository.findAll();
        for (Event event : allEvents) {
            boolean changed = false;

            if (event.getVenue() == null) {
                event.setVenue(defaultVenue);
                changed = true;
            }
            if (isBlank(event.getDescription())) {
                event.setDescription(DEFAULT_EVENT_DESCRIPTION);
                changed = true;
            }
            if (isBlank(event.getImageUrl())) {
                event.setImageUrl(DEFAULT_EVENT_IMAGE);
                changed = true;
            }
            if (event.getEventDate() == null) {
                event.setEventDate(LocalDateTime.now().plusDays(21));
                changed = true;
            }

            if (changed) {
                eventRepository.save(event);
            }
        }
    }

    private void seedSeatsForSectionIfMissing(Section section) {
        if (seatRepository.countBySectionId(section.getId()) > 0) {
            return;
        }

        if (section.getTotalRows() == null || section.getTotalColumns() == null) {
            return;
        }

        List<Seat> seats = generateSeats(section, section.getTotalRows(), section.getTotalColumns());
        seatRepository.saveAll(seats);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
