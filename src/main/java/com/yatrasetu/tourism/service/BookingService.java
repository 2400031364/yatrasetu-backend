package com.yatrasetu.tourism.service;

import com.yatrasetu.tourism.dto.BookingDto;
import com.yatrasetu.tourism.dto.BookingRequest;
import com.yatrasetu.tourism.dto.HotelBookingDto;
import com.yatrasetu.tourism.dto.PaymentRequest;
import com.yatrasetu.tourism.entity.Booking;
import com.yatrasetu.tourism.entity.Destination;
import com.yatrasetu.tourism.entity.Hotel;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.exception.BadRequestException;
import com.yatrasetu.tourism.exception.ResourceNotFoundException;
import com.yatrasetu.tourism.repository.BookingRepository;
import com.yatrasetu.tourism.repository.DestinationRepository;
import com.yatrasetu.tourism.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final DestinationRepository destinationRepository;

    public BookingService(BookingRepository bookingRepository, HotelRepository hotelRepository,
                           DestinationRepository destinationRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.destinationRepository = destinationRepository;
    }

    @Transactional
    public BookingDto create(BookingRequest request, User user) {
        if (request.getHotelId() == null && request.getDestinationId() == null) {
            throw new BadRequestException("Provide at least a hotelId or a destinationId to book");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuests(request.getGuests() != null ? request.getGuests() : 1);
        booking.setTotalAmount(request.getTotalAmount() != null ? request.getTotalAmount() : 0);

        if (request.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + request.getHotelId()));
            booking.setHotel(hotel);
            booking.setDestination(hotel.getDestination());
        } else {
            Destination destination = destinationRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + request.getDestinationId()));
            booking.setDestination(destination);
        }

        bookingRepository.save(booking);
        return toDto(booking);
    }

    @Transactional
    public BookingDto pay(Long id, PaymentRequest request, User user) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only pay for your own bookings");
        }
        if (!"PENDING_PAYMENT".equals(booking.getStatus())) {
            throw new BadRequestException("This booking is not awaiting payment");
        }
        booking.setPaymentMethod(request.getMethod());
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
        return toDto(booking);
    }

    public List<BookingDto> mine(User user) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toDto).toList();
    }

    /**
     * Bookings for the hotel this manager account is assigned to, including
     * the guest's contact details so the hotel can reach out to them.
     */
    public List<HotelBookingDto> forHotelManager(User manager) {
        if (manager.getHotel() == null) {
            throw new BadRequestException("Your account isn't assigned to a hotel yet");
        }
        return bookingRepository.findByHotelIdOrderByCreatedAtDesc(manager.getHotel().getId())
                .stream().map(this::toHotelBookingDto).toList();
    }

    @Transactional
    public void cancel(Long id, User user) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only cancel your own bookings");
        }
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    private BookingDto toDto(Booking b) {
        BookingDto dto = new BookingDto();
        dto.setId(b.getId());
        if (b.getHotel() != null) {
            dto.setHotelId(b.getHotel().getId());
            dto.setHotelName(b.getHotel().getName());
        }
        if (b.getDestination() != null) {
            dto.setDestinationId(b.getDestination().getId());
            dto.setDestinationName(b.getDestination().getName());
        }
        dto.setCheckIn(b.getCheckIn());
        dto.setCheckOut(b.getCheckOut());
        dto.setGuests(b.getGuests());
        dto.setTotalAmount(b.getTotalAmount());
        dto.setPaymentMethod(b.getPaymentMethod());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }

    private HotelBookingDto toHotelBookingDto(Booking b) {
        HotelBookingDto dto = new HotelBookingDto();
        dto.setId(b.getId());
        dto.setGuestName(b.getUser().getName());
        dto.setGuestEmail(b.getUser().getEmail());
        dto.setGuestMobile(b.getUser().getMobile());
        dto.setCheckIn(b.getCheckIn());
        dto.setCheckOut(b.getCheckOut());
        dto.setGuests(b.getGuests());
        dto.setTotalAmount(b.getTotalAmount());
        dto.setPaymentMethod(b.getPaymentMethod());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}
