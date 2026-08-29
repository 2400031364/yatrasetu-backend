package com.yatrasetu.tourism.dto;

import java.util.List;

public class ItineraryRequest {
    private List<ItineraryItemRequest> items;

    public List<ItineraryItemRequest> getItems() { return items; }
    public void setItems(List<ItineraryItemRequest> items) { this.items = items; }
}
