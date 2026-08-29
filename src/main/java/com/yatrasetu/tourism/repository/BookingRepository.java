package com.yatrasetu.tourism.repository;

import com.yatrasetu.tourism.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByHotelIdOrderByCreatedAtDesc(Long hotelId);
}
