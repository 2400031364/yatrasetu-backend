package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.HotelDto;
import com.yatrasetu.tourism.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> list() {
        return ResponseEntity.ok(hotelService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.get(id));
    }

    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<List<HotelDto>> byDestination(@PathVariable Long destinationId) {
        return ResponseEntity.ok(hotelService.byDestination(destinationId));
    }
}
