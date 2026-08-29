package com.yatrasetu.tourism.repository;

import com.yatrasetu.tourism.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    List<Itinerary> findByUserIdOrderByCreatedAtDesc(Long userId);
}
