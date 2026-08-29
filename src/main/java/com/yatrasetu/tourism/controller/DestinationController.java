package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.DestinationDto;
import com.yatrasetu.tourism.service.DestinationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public ResponseEntity<List<DestinationDto>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(destinationService.list(category, q));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<DestinationDto>> featured() {
        return ResponseEntity.ok(destinationService.featured());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(destinationService.categories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinationDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(destinationService.get(id));
    }
}
