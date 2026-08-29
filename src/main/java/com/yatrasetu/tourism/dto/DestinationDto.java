package com.yatrasetu.tourism.dto;

public class DestinationDto {
    private Long id;
    private String name;
    private String state;
    private String category;
    private String tagline;
    private String description;
    private Double rating;
    private Integer reviewCount;
    private Integer priceFrom;
    private String image;
    private String bestSeason;
    private Double lat;
    private Double lng;
    private Boolean featured;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public Integer getPriceFrom() { return priceFrom; }
    public void setPriceFrom(Integer priceFrom) { this.priceFrom = priceFrom; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getBestSeason() { return bestSeason; }
    public void setBestSeason(String bestSeason) { this.bestSeason = bestSeason; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
}
