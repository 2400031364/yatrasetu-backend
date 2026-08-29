package com.yatrasetu.tourism.repository;

import com.yatrasetu.tourism.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {
}
