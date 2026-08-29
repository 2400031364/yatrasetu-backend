package com.yatrasetu.tourism.service;

import com.yatrasetu.tourism.dto.DestinationDto;
import com.yatrasetu.tourism.entity.Destination;
import com.yatrasetu.tourism.exception.ResourceNotFoundException;
import com.yatrasetu.tourism.repository.DestinationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public DestinationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public List<DestinationDto> list(String category, String q) {
        List<Destination> results;
        if (q != null && !q.isBlank()) {
            results = destinationRepository.findByNameContainingIgnoreCaseOrStateContainingIgnoreCase(q, q);
        } else if (category != null && !category.isBlank()) {
            results = destinationRepository.findByCategory(category);
        } else {
            results = destinationRepository.findAll();
        }
        if (category != null && !category.isBlank() && (q != null && !q.isBlank())) {
            results = results.stream().filter(d -> d.getCategory().equalsIgnoreCase(category)).toList();
        }
        return results.stream().map(this::toDto).toList();
    }

    public List<DestinationDto> featured() {
        return destinationRepository.findByFeaturedTrue().stream().map(this::toDto).toList();
    }

    public List<String> categories() {
        return destinationRepository.findAll().stream()
                .map(Destination::getCategory).distinct().toList();
    }

    public DestinationDto get(Long id) {
        Destination d = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + id));
        return toDto(d);
    }

    Destination getEntity(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + id));
    }

    private DestinationDto toDto(Destination d) {
        DestinationDto dto = new DestinationDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setState(d.getState());
        dto.setCategory(d.getCategory());
        dto.setTagline(d.getTagline());
        dto.setDescription(d.getDescription());
        dto.setRating(d.getRating());
        dto.setReviewCount(d.getReviewCount());
        dto.setPriceFrom(d.getPriceFrom());
        dto.setImage(d.getImage());
        dto.setBestSeason(d.getBestSeason());
        dto.setLat(d.getLat());
        dto.setLng(d.getLng());
        dto.setFeatured(d.getFeatured());
        return dto;
    }
}
