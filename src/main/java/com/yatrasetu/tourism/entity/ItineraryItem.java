package com.yatrasetu.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "itinerary_items")
public class ItineraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(nullable = false)
    private Integer day;

    @Column(nullable = false)
    private Integer nights = 1;

    public ItineraryItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Itinerary getItinerary() { return itinerary; }
    public void setItinerary(Itinerary itinerary) { this.itinerary = itinerary; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public Integer getDay() { return day; }
    public void setDay(Integer day) { this.day = day; }

    public Integer getNights() { return nights; }
    public void setNights(Integer nights) { this.nights = nights; }
}
