package com.yatrasetu.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "destinations")
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String state;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(length = 255)
    private String tagline;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "price_from", nullable = false)
    private Integer priceFrom;

    @Column(length = 500)
    private String image;

    @Column(name = "best_season", length = 60)
    private String bestSeason;

    private Double lat;
    private Double lng;

    @Column(nullable = false)
    private Boolean featured = false;

    public Destination() {}

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
