package com.yatrasetu.tourism.dto;

import java.util.List;

public class PlaceSearchResponse {
    private String city;
    private Double centerLat;
    private Double centerLng;
    private List<TouristPlaceDto> places;

    public PlaceSearchResponse() {}

    public PlaceSearchResponse(String city, Double centerLat, Double centerLng, List<TouristPlaceDto> places) {
        this.city = city;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.places = places;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getCenterLat() { return centerLat; }
    public void setCenterLat(Double centerLat) { this.centerLat = centerLat; }
    public Double getCenterLng() { return centerLng; }
    public void setCenterLng(Double centerLng) { this.centerLng = centerLng; }
    public List<TouristPlaceDto> getPlaces() { return places; }
    public void setPlaces(List<TouristPlaceDto> places) { this.places = places; }
}
