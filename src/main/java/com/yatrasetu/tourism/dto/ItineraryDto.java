package com.yatrasetu.tourism.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ItineraryDto {
    private Long id;
    private LocalDateTime createdAt;
    private List<ItineraryItemDto> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ItineraryItemDto> getItems() { return items; }
    public void setItems(List<ItineraryItemDto> items) { this.items = items; }
}
