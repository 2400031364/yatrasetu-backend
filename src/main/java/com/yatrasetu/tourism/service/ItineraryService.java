package com.yatrasetu.tourism.service;

import com.yatrasetu.tourism.dto.*;
import com.yatrasetu.tourism.entity.Destination;
import com.yatrasetu.tourism.entity.Itinerary;
import com.yatrasetu.tourism.entity.ItineraryItem;
import com.yatrasetu.tourism.entity.User;
import com.yatrasetu.tourism.exception.BadRequestException;
import com.yatrasetu.tourism.exception.ResourceNotFoundException;
import com.yatrasetu.tourism.repository.DestinationRepository;
import com.yatrasetu.tourism.repository.ItineraryItemRepository;
import com.yatrasetu.tourism.repository.ItineraryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final ItineraryItemRepository itineraryItemRepository;
    private final DestinationRepository destinationRepository;

    public ItineraryService(ItineraryRepository itineraryRepository,
                             ItineraryItemRepository itineraryItemRepository,
                             DestinationRepository destinationRepository) {
        this.itineraryRepository = itineraryRepository;
        this.itineraryItemRepository = itineraryItemRepository;
        this.destinationRepository = destinationRepository;
    }

    public List<ItineraryDto> mine(User user) {
        return itineraryRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toDto).toList();
    }

    @Transactional
    public ItineraryDto create(ItineraryRequest request, User user) {
        Itinerary itinerary = new Itinerary();
        itinerary.setUser(user);
        itineraryRepository.save(itinerary);

        if (request.getItems() != null) {
            int day = 1;
            for (ItineraryItemRequest itemReq : request.getItems()) {
                Destination destination = destinationRepository.findById(itemReq.getDestinationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + itemReq.getDestinationId()));
                ItineraryItem item = new ItineraryItem();
                item.setItinerary(itinerary);
                item.setDestination(destination);
                item.setDay(itemReq.getDay() != null ? itemReq.getDay() : day);
                item.setNights(itemReq.getNights() != null ? itemReq.getNights() : 1);
                itineraryItemRepository.save(item);
                itinerary.getItems().add(item);
                day++;
            }
        }

        return toDto(itinerary);
    }

    @Transactional
    public ItineraryItemDto addItem(Long itineraryId, ItineraryItemRequest request, User user) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found: " + itineraryId));
        assertOwner(itinerary, user);

        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + request.getDestinationId()));

        ItineraryItem item = new ItineraryItem();
        item.setItinerary(itinerary);
        item.setDestination(destination);
        item.setDay(request.getDay() != null ? request.getDay() : itinerary.getItems().size() + 1);
        item.setNights(request.getNights() != null ? request.getNights() : 1);
        itineraryItemRepository.save(item);

        return toItemDto(item);
    }

    @Transactional
    public void removeItem(Long itineraryId, Long itemId, User user) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found: " + itineraryId));
        assertOwner(itinerary, user);
        itineraryItemRepository.deleteById(itemId);
    }

    @Transactional
    public void delete(Long itineraryId, User user) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found: " + itineraryId));
        assertOwner(itinerary, user);
        itineraryRepository.delete(itinerary);
    }

    private void assertOwner(Itinerary itinerary, User user) {
        if (!itinerary.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only modify your own itinerary");
        }
    }

    private ItineraryDto toDto(Itinerary itinerary) {
        ItineraryDto dto = new ItineraryDto();
        dto.setId(itinerary.getId());
        dto.setCreatedAt(itinerary.getCreatedAt());
        dto.setItems(itinerary.getItems().stream().map(this::toItemDto).toList());
        return dto;
    }

    private ItineraryItemDto toItemDto(ItineraryItem item) {
        ItineraryItemDto dto = new ItineraryItemDto();
        dto.setId(item.getId());
        dto.setDestinationId(item.getDestination().getId());
        dto.setDestinationName(item.getDestination().getName());
        dto.setImage(item.getDestination().getImage());
        dto.setDay(item.getDay());
        dto.setNights(item.getNights());
        return dto;
    }
}
