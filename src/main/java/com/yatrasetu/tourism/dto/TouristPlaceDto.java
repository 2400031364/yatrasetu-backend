package com.yatrasetu.tourism.dto;

import java.util.List;

public class TouristPlaceDto {
    private String name;
    private String category;
    private Double lat;
    private Double lng;
    private Double distanceFromCenterKm;
    private List<NearbyStayDto> nearbyStays;

    public TouristPlaceDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getDistanceFromCenterKm() { return distanceFromCenterKm; }
    public void setDistanceFromCenterKm(Double distanceFromCenterKm) { this.distanceFromCenterKm = distanceFromCenterKm; }
    public List<NearbyStayDto> getNearbyStays() { return nearbyStays; }
    public void setNearbyStays(List<NearbyStayDto> nearbyStays) { this.nearbyStays = nearbyStays; }
}
