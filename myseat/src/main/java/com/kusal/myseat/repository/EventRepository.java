package com.kusal.myseat.repository;

import com.kusal.myseat.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
	List<Event> findAllByOrderByEventDateAsc();
}