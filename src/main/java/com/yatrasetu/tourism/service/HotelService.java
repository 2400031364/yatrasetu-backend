package com.yatrasetu.tourism.service;

import com.yatrasetu.tourism.dto.HotelDto;
import com.yatrasetu.tourism.entity.Hotel;
import com.yatrasetu.tourism.exception.ResourceNotFoundException;
import com.yatrasetu.tourism.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public List<HotelDto> list() {
        return hotelRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<HotelDto> byDestination(Long destinationId) {
        return hotelRepository.findByDestinationId(destinationId).stream().map(this::toDto).toList();
    }

    public HotelDto get(Long id) {
        Hotel h = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
        return toDto(h);
    }

    Hotel getEntity(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }

    private HotelDto toDto(Hotel h) {
        HotelDto dto = new HotelDto();
        dto.setId(h.getId());
        dto.setDestinationId(h.getDestination().getId());
        dto.setName(h.getName());
        dto.setStars(h.getStars());
        dto.setPricePerNight(h.getPricePerNight());
        dto.setRating(h.getRating());
        dto.setReviewCount(h.getReviewCount());
        dto.setImage(h.getImage());
        dto.setAmenities(h.getAmenities());
        return dto;
    }
}
