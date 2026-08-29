package com.yatrasetu.tourism.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ImportPlaceRequest {
    @NotBlank(message = "city is required")
    private String city;

    @NotNull(message = "place is required")
    @Valid
    private TouristPlaceDto place;

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public TouristPlaceDto getPlace() { return place; }
    public void setPlace(TouristPlaceDto place) { this.place = place; }
}
