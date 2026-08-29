package com.yatrasetu.tourism.repository;

import com.yatrasetu.tourism.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByDestinationId(Long destinationId);
    boolean existsByDestinationIdAndName(Long destinationId, String name);
}
