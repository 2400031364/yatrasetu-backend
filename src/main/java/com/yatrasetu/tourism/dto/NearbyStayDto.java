package com.yatrasetu.tourism.dto;

public class NearbyStayDto {
    private String name;
    private String type;
    private Double lat;
    private Double lng;
    private Double distanceKm;

    public NearbyStayDto() {}

    public NearbyStayDto(String name, String type, Double lat, Double lng, Double distanceKm) {
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.distanceKm = distanceKm;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
