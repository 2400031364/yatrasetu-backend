package com.yatrasetu.tourism.dto;

import java.util.List;

public class RoutePlanResponse {
    private RoutePointDto source;
    private RoutePointDto destination;
    private Double distanceKm;
    private Double durationMinutes;
    // Road route geometry, as [lat, lng] pairs in travel order — ready to
    // hand straight to a map polyline on the frontend.
    private List<double[]> geometry;
    // Notable tourist places found along the way, ordered by how far into
    // the journey a traveller would reach them.
    private List<RouteStopDto> places;

    public RoutePlanResponse() {}

    public RoutePlanResponse(RoutePointDto source, RoutePointDto destination, Double distanceKm,
                              Double durationMinutes, List<double[]> geometry, List<RouteStopDto> places) {
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.geometry = geometry;
        this.places = places;
    }

    public RoutePointDto getSource() { return source; }
    public void setSource(RoutePointDto source) { this.source = source; }
    public RoutePointDto getDestination() { return destination; }
    public void setDestination(RoutePointDto destination) { this.destination = destination; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Double durationMinutes) { this.durationMinutes = durationMinutes; }
    public List<double[]> getGeometry() { return geometry; }
    public void setGeometry(List<double[]> geometry) { this.geometry = geometry; }
    public List<RouteStopDto> getPlaces() { return places; }
    public void setPlaces(List<RouteStopDto> places) { this.places = places; }
}
