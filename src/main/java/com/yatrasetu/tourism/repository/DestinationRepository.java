package com.yatrasetu.tourism.repository;

import com.yatrasetu.tourism.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByFeaturedTrue();
    List<Destination> findByCategory(String category);
    List<Destination> findByNameContainingIgnoreCaseOrStateContainingIgnoreCase(String name, String state);
    Optional<Destination> findByName(String name);

    // Used when "importing" a live-searched place: treat anything within
    // roughly 300m of an existing destination as the same place, so
    // clicking the same OpenStreetMap result twice doesn't create duplicates.
    @Query("SELECT d FROM Destination d WHERE ABS(d.lat - :lat) < 0.003 AND ABS(d.lng - :lng) < 0.003")
    List<Destination> findNear(@Param("lat") double lat, @Param("lng") double lng);
}
