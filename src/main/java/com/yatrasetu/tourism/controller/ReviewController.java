package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.ReviewDto;
import com.yatrasetu.tourism.dto.ReviewRequest;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<List<ReviewDto>> byDestination(@PathVariable Long destinationId) {
        return ResponseEntity.ok(reviewService.byDestination(destinationId));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewRequest request,
                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.create(request, user));
    }
}
