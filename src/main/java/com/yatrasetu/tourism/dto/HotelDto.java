package com.yatrasetu.tourism.dto;

import java.util.List;

public class HotelDto {
    private Long id;
    private Long destinationId;
    private String name;
    private Integer stars;
    private Integer pricePerNight;
    private Double rating;
    private Integer reviewCount;
    private String image;
    private List<String> amenities;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDestinationId() { return destinationId; }
    public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public Integer getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(Integer pricePerNight) { this.pricePerNight = pricePerNight; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
}
