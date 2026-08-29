package com.yatrasetu.tourism.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A booking as seen by the hotel manager dashboard: unlike BookingDto (used
 * by the guest themselves), this includes the guest's contact details so
 * hotel staff can reach out about the stay.
 */
public class HotelBookingDto {
    private Long id;
    private String guestName;
    private String guestEmail;
    private String guestMobile;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;
    private Integer totalAmount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public String getGuestMobile() { return guestMobile; }
    public void setGuestMobile(String guestMobile) { this.guestMobile = guestMobile; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }
    public Integer getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Integer totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
