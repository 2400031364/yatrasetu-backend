package com.yatrasetu.tourism.dto;

public class ItineraryItemDto {
    private Long id;
    private Long destinationId;
    private String destinationName;
    private String image;
    private Integer day;
    private Integer nights;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDestinationId() { return destinationId; }
    public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }
    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Integer getDay() { return day; }
    public void setDay(Integer day) { this.day = day; }
    public Integer getNights() { return nights; }
    public void setNights(Integer nights) { this.nights = nights; }
}
