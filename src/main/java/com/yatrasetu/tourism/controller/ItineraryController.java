package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.*;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.service.ItineraryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<ItineraryDto>> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itineraryService.mine(user));
    }

    @PostMapping
    public ResponseEntity<ItineraryDto> create(@RequestBody ItineraryRequest request,
                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itineraryService.create(request, user));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ItineraryItemDto> addItem(@PathVariable Long id,
                                                      @Valid @RequestBody ItineraryItemRequest request,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itineraryService.addItem(id, request, user));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId,
                                            @AuthenticationPrincipal User user) {
        itineraryService.removeItem(id, itemId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        itineraryService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
