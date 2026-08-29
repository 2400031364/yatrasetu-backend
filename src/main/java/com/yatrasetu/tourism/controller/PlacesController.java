package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.ImportPlaceRequest;
import com.yatrasetu.tourism.dto.ImportPlaceResponse;
import com.yatrasetu.tourism.dto.PlaceSearchResponse;
import com.yatrasetu.tourism.service.PlacesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
public class PlacesController {

    private final PlacesService placesService;

    public PlacesController(PlacesService placesService) {
        this.placesService = placesService;
    }

    /**
     * Live tourist attractions + nearby stays for any city, fetched from
     * OpenStreetMap in real time — not limited to the curated demo list.
     * Example: GET /api/places/search?city=Jaipur
     */
    @GetMapping("/search")
    public ResponseEntity<PlaceSearchResponse> search(@RequestParam String city) {
        return ResponseEntity.ok(placesService.search(city));
    }

    /**
     * "Promotes" a clicked live search result into a real Destination (and
     * its nearby stays into real Hotels), so it gets a full detail page,
     * reviews and the existing booking/payment flow for free.
     */
    @PostMapping("/import")
    public ResponseEntity<ImportPlaceResponse> importPlace(@Valid @RequestBody ImportPlaceRequest request) {
        return ResponseEntity.ok(placesService.importPlace(request));
    }
}
