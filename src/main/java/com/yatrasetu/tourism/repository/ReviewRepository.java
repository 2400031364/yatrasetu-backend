package com.yatrasetu.tourism.repository;

import com.yatrasetu.tourism.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByDestinationIdOrderByDateDesc(Long destinationId);
}
