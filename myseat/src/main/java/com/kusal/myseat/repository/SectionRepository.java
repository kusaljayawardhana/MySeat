package com.kusal.myseat.repository;

import com.kusal.myseat.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
	List<Section> findByVenueId(Long venueId);
}