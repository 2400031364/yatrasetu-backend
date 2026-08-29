package com.yatrasetu.tourism.service;

import com.yatrasetu.tourism.dto.ReviewDto;
import com.yatrasetu.tourism.dto.ReviewRequest;
import com.yatrasetu.tourism.entity.Destination;
import com.yatrasetu.tourism.entity.Review;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.repository.DestinationRepository;
import com.yatrasetu.tourism.repository.ReviewRepository;
import com.yatrasetu.tourism.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DestinationRepository destinationRepository;

    public ReviewService(ReviewRepository reviewRepository, DestinationRepository destinationRepository) {
        this.reviewRepository = reviewRepository;
        this.destinationRepository = destinationRepository;
    }

    public List<ReviewDto> byDestination(Long destinationId) {
        return reviewRepository.findByDestinationIdOrderByDateDesc(destinationId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ReviewDto create(ReviewRequest request, User user) {
        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + request.getDestinationId()));

        Review review = new Review();
        review.setDestination(destination);
        review.setUser(user);
        review.setAuthor(user != null ? user.getName() : "Guest traveller");
        review.setRating(request.getRating());
        review.setVerified(user != null);
        review.setText(request.getText());
        reviewRepository.save(review);

        // keep the destination's aggregate rating roughly in sync
        int newCount = destination.getReviewCount() + 1;
        double newRating = ((destination.getRating() * destination.getReviewCount()) + request.getRating()) / newCount;
        destination.setReviewCount(newCount);
        destination.setRating(Math.round(newRating * 10.0) / 10.0);
        destinationRepository.save(destination);

        return toDto(review);
    }

    private ReviewDto toDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setId(r.getId());
        dto.setDestinationId(r.getDestination().getId());
        dto.setAuthor(r.getAuthor());
        dto.setRating(r.getRating());
        dto.setVerified(r.getVerified());
        dto.setDate(r.getDate());
        dto.setText(r.getText());
        return dto;
    }
}
