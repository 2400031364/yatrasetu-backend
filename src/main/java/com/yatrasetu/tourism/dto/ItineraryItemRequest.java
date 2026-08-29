package com.yatrasetu.tourism.dto;

import jakarta.validation.constraints.NotNull;

public class ItineraryItemRequest {
    @NotNull(message = "destinationId is required")
    private Long destinationId;
    private Integer day = 1;
    private Integer nights = 1;

    public Long getDestinationId() { return destinationId; }
    public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }
    public Integer getDay() { return day; }
    public void setDay(Integer day) { this.day = day; }
    public Integer getNights() { return nights; }
    public void setNights(Integer nights) { this.nights = nights; }
}
