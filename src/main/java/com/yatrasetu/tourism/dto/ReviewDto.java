package com.yatrasetu.tourism.dto;

import java.time.LocalDate;

public class ReviewDto {
    private Long id;
    private Long destinationId;
    private String author;
    private Integer rating;
    private Boolean verified;
    private LocalDate date;
    private String text;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDestinationId() { return destinationId; }
    public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
