package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.BookingDto;
import com.yatrasetu.tourism.dto.BookingRequest;
import com.yatrasetu.tourism.dto.HotelBookingDto;
import com.yatrasetu.tourism.dto.PaymentRequest;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingDto> create(@RequestBody BookingRequest request,
                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.create(request, user));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<BookingDto> pay(@PathVariable Long id,
                                           @Valid @RequestBody PaymentRequest request,
                                           @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.pay(id, request, user));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingDto>> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.mine(user));
    }

    // Hotel-manager dashboard: bookings for the manager's own hotel, with
    // guest contact details so the hotel can get in touch. Restricted to
    // ROLE_HOTEL_MANAGER accounts at the security-filter level too.
    @GetMapping("/hotel-manager")
    public ResponseEntity<List<HotelBookingDto>> forHotelManager(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.forHotelManager(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal User user) {
        bookingService.cancel(id, user);
        return ResponseEntity.noContent().build();
    }
}
