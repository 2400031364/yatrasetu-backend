package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.*;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/request")
    public ResponseEntity<MessageResponse> requestOtp(@Valid @RequestBody OtpRequestRequest request) {
        authService.requestOtp(request);
        return ResponseEntity.ok(new MessageResponse("A 6-digit code has been sent to " + request.getEmail()));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.me(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@Valid @RequestBody UpdateProfileRequest request,
                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.updateProfile(user, request));
    }
}
