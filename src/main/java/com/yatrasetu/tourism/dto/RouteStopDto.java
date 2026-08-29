package com.yatrasetu.tourism.dto;

public class RouteStopDto {
    private String name;
    private String category;
    private Double lat;
    private Double lng;
    // How far along the journey (from the source, following the road route)
    // this place roughly sits — used to order stops the way a traveller
    // would actually encounter them, not just alphabetically.
    private Double distanceFromStartKm;

    public RouteStopDto() {}

    public RouteStopDto(String name, String category, Double lat, Double lng, Double distanceFromStartKm) {
        this.name = name;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        this.distanceFromStartKm = distanceFromStartKm;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getDistanceFromStartKm() { return distanceFromStartKm; }
    public void setDistanceFromStartKm(Double distanceFromStartKm) { this.distanceFromStartKm = distanceFromStartKm; }
}
