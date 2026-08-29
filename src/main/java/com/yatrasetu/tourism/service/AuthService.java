package com.yatrasetu.tourism.service;

import com.yatrasetu.tourism.dto.*;
import com.yatrasetu.tourism.entity.Hotel;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.exception.BadRequestException;
import com.yatrasetu.tourism.repository.HotelRepository;
import com.yatrasetu.tourism.repository.UserRepository;
import com.yatrasetu.tourism.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final int OTP_VALID_MINUTES = 5;

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository, HotelRepository hotelRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists. Try signing in instead.");
        }
        User user = new User(request.getName(), request.getEmail(), passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);

        String role = request.getRole();
        if (role != null && role.equalsIgnoreCase("HOTEL_MANAGER")) {
            if (request.getHotelId() == null) {
                throw new BadRequestException("Select the hotel you manage to register as a hotel manager");
            }
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new BadRequestException("Selected hotel was not found"));
            user.setRole("HOTEL_MANAGER");
            user.setHotel(hotel);
        }

        sendOtpTo(user);
        userRepository.save(user);
        return new MessageResponse("We've sent a 6-digit code to " + request.getEmail() + " to verify your account.");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Incorrect email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toDto(user));
    }

    // ---- OTP sign-in ----

    @Transactional
    public void requestOtp(OtpRequestRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("No account found with this email. Please sign up first."));

        sendOtpTo(user);
        userRepository.save(user);
    }

    private void sendOtpTo(User user) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        user.setOtpCode(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
        emailService.sendOtp(user.getEmail(), otp);
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("No account found with this email."));

        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null) {
            throw new BadRequestException("No OTP was requested for this email. Request a new code.");
        }
        if (LocalDateTime.now().isAfter(user.getOtpExpiresAt())) {
            throw new BadRequestException("This code has expired. Request a new one.");
        }
        if (!user.getOtpCode().equals(request.getOtp().trim())) {
            throw new BadRequestException("Incorrect code. Check your email and try again.");
        }

        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        user.setEmailVerified(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toDto(user));
    }

    // ---- Profile ----

    public UserDto me(User user) {
        return toDto(user);
    }

    @Transactional
    public UserDto updateProfile(User user, UpdateProfileRequest request) {
        user.setName(request.getName());
        user.setMobile(request.getMobile());
        user.setAddress(request.getAddress());
        userRepository.save(user);
        return toDto(user);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getMobile(), user.getAddress());
        dto.setRole(user.getRole());
        if (user.getHotel() != null) {
            dto.setHotelId(user.getHotel().getId());
            dto.setHotelName(user.getHotel().getName());
        }
        return dto;
    }
}
