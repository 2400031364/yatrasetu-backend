package com.yatrasetu.tourism.dto;

import java.util.List;

public class ImportPlaceResponse {
    private DestinationDto destination;
    private List<HotelDto> hotels;

    public ImportPlaceResponse() {}

    public ImportPlaceResponse(DestinationDto destination, List<HotelDto> hotels) {
        this.destination = destination;
        this.hotels = hotels;
    }

    public DestinationDto getDestination() { return destination; }
    public void setDestination(DestinationDto destination) { this.destination = destination; }
    public List<HotelDto> getHotels() { return hotels; }
    public void setHotels(List<HotelDto> hotels) { this.hotels = hotels; }
}
