package com.yatrasetu.tourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatrasetu.tourism.dto.DestinationDto;
import com.yatrasetu.tourism.dto.HotelDto;
import com.yatrasetu.tourism.dto.ImportPlaceRequest;
import com.yatrasetu.tourism.dto.ImportPlaceResponse;
import com.yatrasetu.tourism.dto.NearbyStayDto;
import com.yatrasetu.tourism.dto.PlaceSearchResponse;
import com.yatrasetu.tourism.dto.TouristPlaceDto;
import com.yatrasetu.tourism.entity.Destination;
import com.yatrasetu.tourism.entity.Hotel;
import com.yatrasetu.tourism.exception.BadRequestException;
import com.yatrasetu.tourism.exception.ResourceNotFoundException;
import com.yatrasetu.tourism.repository.DestinationRepository;
import com.yatrasetu.tourism.repository.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * Fetches REAL, live tourist attractions and nearby stays for any city the
 * user searches, using two free OpenStreetMap services (no API key needed):
 *   1. Nominatim  — turns a city name into latitude/longitude.
 *   2. Overpass   — returns actual named attractions + lodging around that
 *                    point, straight from OpenStreetMap's live map data.
 * Distances are computed locally with the Haversine formula.
 */
@Service
public class PlacesService {

    private static final Logger log = LoggerFactory.getLogger(PlacesService.class);

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    // Independent public Overpass mirrors — same fail-over strategy as
    // RouteService, so one mirror being down doesn't break live search.
    private static final List<String> OVERPASS_MIRRORS = List.of(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://overpass.openstreetmap.ru/api/interpreter"
    );

    // Radius to search for BOTH attractions and lodging around the city centre.
    private static final int SEARCH_RADIUS_METERS = 15_000;
    // How far a stay can be from an attraction to count as "nearby".
    private static final double MAX_STAY_DISTANCE_KM = 6.0;
    private static final int MAX_STAYS_PER_PLACE = 6;

    private final ExternalApiClient apiClient;
    private final ObjectMapper mapper = new ObjectMapper();

    private final DestinationRepository destinationRepository;
    private final HotelRepository hotelRepository;

    public PlacesService(ExternalApiClient apiClient, DestinationRepository destinationRepository,
                          HotelRepository hotelRepository) {
        this.apiClient = apiClient;
        this.destinationRepository = destinationRepository;
        this.hotelRepository = hotelRepository;
    }

    private static final Set<String> STAY_TOURISM_VALUES = Set.of(
            "hotel", "hostel", "guest_house", "motel", "apartment", "chalet", "alpine_hut", "resort"
    );
    private static final Set<String> PLACE_TOURISM_VALUES = Set.of(
            "attraction", "museum", "viewpoint", "zoo", "theme_park", "gallery", "artwork", "aquarium"
    );

    public PlaceSearchResponse search(String city) {
        if (city == null || city.isBlank()) {
            throw new BadRequestException("Enter a city name to search.");
        }

        double[] center = geocode(city.trim());
        JsonNode elements = queryOverpass(center[0], center[1]);

        List<RawNode> places = new ArrayList<>();
        List<RawNode> stays = new ArrayList<>();

        for (JsonNode el : elements) {
            JsonNode tags = el.path("tags");
            if (!tags.has("name")) continue; // skip unnamed nodes — not useful to show

            double lat = el.has("lat") ? el.get("lat").asDouble() : el.path("center").path("lat").asDouble();
            double lon = el.has("lon") ? el.get("lon").asDouble() : el.path("center").path("lon").asDouble();
            if (lat == 0 && lon == 0) continue;

            String name = tags.get("name").asText();
            String tourism = tags.path("tourism").asText("");
            boolean isHistoric = tags.has("historic");
            boolean isBeach = "beach".equals(tags.path("natural").asText(""));
            boolean isPark = "park".equals(tags.path("leisure").asText(""));

            if (STAY_TOURISM_VALUES.contains(tourism)) {
                stays.add(new RawNode(name, tourism, lat, lon));
            } else if (PLACE_TOURISM_VALUES.contains(tourism) || isHistoric || isBeach || isPark) {
                String category = isHistoric ? "Historic site"
                        : isBeach ? "Beach"
                        : isPark ? "Park"
                        : prettify(tourism);
                places.add(new RawNode(name, category, lat, lon));
            }
        }

        dedupeByName(places);
        dedupeByName(stays);

        List<TouristPlaceDto> placeDtos = new ArrayList<>();
        for (RawNode place : places) {
            TouristPlaceDto dto = new TouristPlaceDto();
            dto.setName(place.name);
            dto.setCategory(place.category);
            dto.setLat(place.lat);
            dto.setLng(place.lon);
            dto.setDistanceFromCenterKm(round1(haversineKm(center[0], center[1], place.lat, place.lon)));

            List<NearbyStayDto> nearby = new ArrayList<>();
            for (RawNode stay : stays) {
                double d = haversineKm(place.lat, place.lon, stay.lat, stay.lon);
                if (d <= MAX_STAY_DISTANCE_KM) {
                    nearby.add(new NearbyStayDto(stay.name, prettify(stay.category), stay.lat, stay.lon, round1(d)));
                }
            }
            nearby.sort(Comparator.comparingDouble(NearbyStayDto::getDistanceKm));
            dto.setNearbyStays(nearby.subList(0, Math.min(MAX_STAYS_PER_PLACE, nearby.size())));

            placeDtos.add(dto);
        }

        placeDtos.sort(Comparator.comparingDouble(TouristPlaceDto::getDistanceFromCenterKm));

        return new PlaceSearchResponse(city.trim(), center[0], center[1], placeDtos);
    }

    // ---- Nominatim geocoding ----

    private double[] geocode(String city) {
        String query = URLEncoder.encode(city, StandardCharsets.UTF_8);
        URI uri = URI.create(NOMINATIM_URL + "?q=" + query + "&format=json&limit=1");

        try {
            HttpResponse<String> response = apiClient.send(
                    ExternalApiClient.get(uri, Duration.ofSeconds(12)), "Geocoding \"" + city + "\"");
            JsonNode results = mapper.readTree(response.body());

            if (!results.isArray() || results.isEmpty()) {
                throw new ResourceNotFoundException("Couldn't find a place named \"" + city + "\". Check the spelling and try again.");
            }

            JsonNode first = results.get(0);
            return new double[]{first.get("lat").asDouble(), first.get("lon").asDouble()};

        } catch (ExternalApiClient.ExternalApiException e) {
            log.error("Nominatim geocoding failed for \"{}\": {}", city, e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing geocoding response for \"{}\"", city, e);
            throw new BadRequestException("Live place search is temporarily unavailable. Please try again in a moment.");
        }
    }

    // ---- Overpass query ----

    private JsonNode queryOverpass(double lat, double lon) {
        String query = String.format(Locale.ROOT,
                "[out:json][timeout:25];" +
                "(" +
                "  node[\"tourism\"~\"attraction|museum|viewpoint|zoo|theme_park|gallery|artwork|aquarium\"](around:%d,%f,%f);" +
                "  node[\"historic\"](around:%d,%f,%f);" +
                "  node[\"natural\"=\"beach\"](around:%d,%f,%f);" +
                "  node[\"leisure\"=\"park\"](around:%d,%f,%f);" +
                "  node[\"tourism\"~\"hotel|hostel|guest_house|motel|apartment|chalet|alpine_hut|resort\"](around:%d,%f,%f);" +
                ");" +
                "out body 200;",
                SEARCH_RADIUS_METERS, lat, lon,
                SEARCH_RADIUS_METERS, lat, lon,
                SEARCH_RADIUS_METERS, lat, lon,
                SEARCH_RADIUS_METERS, lat, lon,
                SEARCH_RADIUS_METERS, lat, lon
        );

        String body = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        try {
            HttpResponse<String> response = apiClient.sendWithFallback(
                    OVERPASS_MIRRORS,
                    baseUrl -> HttpRequest.newBuilder(URI.create(baseUrl))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("User-Agent", "YatraSetu-CollegeProject/1.0 (student demo)")
                            .timeout(Duration.ofSeconds(30))
                            .POST(HttpRequest.BodyPublishers.ofString(body)),
                    "Overpass place search");
            JsonNode root = mapper.readTree(response.body());
            return root.path("elements");

        } catch (ExternalApiClient.ExternalApiException e) {
            log.error("Overpass place search failed on every mirror: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error parsing Overpass response", e);
            throw new BadRequestException("Live place search is temporarily unavailable. Please try again in a moment.");
        }
    }

    // ---- Import a live search result into the real catalog ----
    // Clicking a live place "promotes" it into a real Destination row (and
    // its nearby stays into real Hotel rows), reusing the exact same
    // detail page, review system and booking/payment flow that the curated
    // destinations already have — instead of building a parallel system.

    @Transactional
    public ImportPlaceResponse importPlace(ImportPlaceRequest request) {
        TouristPlaceDto place = request.getPlace();
        if (place == null || place.getLat() == null || place.getLng() == null || place.getName() == null) {
            throw new BadRequestException("Missing place details to import.");
        }

        List<Destination> existing = destinationRepository.findNear(place.getLat(), place.getLng());
        Destination destination = existing.stream()
                .filter(d -> d.getName().equalsIgnoreCase(place.getName()))
                .findFirst()
                .orElse(existing.isEmpty() ? null : existing.get(0));

        if (destination == null) {
            destination = new Destination();
            destination.setName(place.getName());
            destination.setState(resolveState(place.getLat(), place.getLng(), request.getCity()));
            destination.setCategory(mapCategory(place.getCategory()));
            destination.setTagline("Discovered via live search near " + request.getCity());
            destination.setDescription("A real place found via live OpenStreetMap search near "
                    + request.getCity() + ". Category: " + place.getCategory() + ".");
            destination.setRating(4.5);
            destination.setReviewCount(0);
            destination.setPriceFrom(2000); // generic estimate — no live pricing source
            destination.setImage(imageForCategory(destination.getCategory()));
            destination.setBestSeason("Year-round");
            destination.setLat(place.getLat());
            destination.setLng(place.getLng());
            destination.setFeatured(false);
            destination = destinationRepository.save(destination);
        }

        List<HotelDto> hotelDtos = new ArrayList<>();
        if (place.getNearbyStays() != null) {
            for (NearbyStayDto stay : place.getNearbyStays()) {
                Hotel hotel = importStay(destination, stay);
                if (hotel != null) hotelDtos.add(toHotelDto(hotel));
            }
        }

        return new ImportPlaceResponse(toDestinationDto(destination), hotelDtos);
    }

    private Hotel importStay(Destination destination, NearbyStayDto stay) {
        if (stay.getName() == null) return null;

        List<Hotel> existingHotels = hotelRepository.findByDestinationId(destination.getId());
        for (Hotel h : existingHotels) {
            if (h.getName().equalsIgnoreCase(stay.getName())) return h;
        }

        String type = stay.getType() == null ? "" : stay.getType().toLowerCase(Locale.ROOT);
        int stars = type.contains("resort") ? 4
                : type.contains("hostel") || type.contains("alpine") ? 2
                : 3;
        int pricePerNight = switch (stars) {
            case 4 -> 3800;
            case 2 -> 1200;
            default -> 2200;
        };

        List<String> amenities = new ArrayList<>(List.of("Free WiFi", "24/7 front desk"));
        if (type.contains("resort")) amenities.add("Pool");
        if (type.contains("hostel")) amenities.add("Common lounge");
        if (type.contains("guest")) amenities.add("Home-cooked meals");

        Hotel hotel = new Hotel();
        hotel.setDestination(destination);
        hotel.setName(stay.getName());
        hotel.setStars(stars);
        hotel.setPricePerNight(pricePerNight);
        hotel.setRating(4.3);
        hotel.setReviewCount(0);
        hotel.setImage("https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=1200");
        hotel.setAmenities(amenities);
        return hotelRepository.save(hotel);
    }

    private String resolveState(double lat, double lng, String fallbackCity) {
        try {
            URI uri = URI.create(String.format(Locale.ROOT,
                    "https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1", lat, lng));
            // Only one attempt here on purpose: this is a "nice to have" —
            // never let a flaky reverse-geocode block importing the place.
            HttpResponse<String> response = apiClient.send(
                    ExternalApiClient.get(uri, Duration.ofSeconds(10)), "Reverse geocoding");
            JsonNode state = mapper.readTree(response.body()).path("address").path("state");
            if (!state.isMissingNode() && !state.asText().isBlank()) return state.asText();
        } catch (Exception ignored) {
            // fall through to the fallback below — never let this block an import
        }
        return fallbackCity;
    }

    private String mapCategory(String liveCategory) {
        if (liveCategory == null) return "offbeat";
        String c = liveCategory.toLowerCase(Locale.ROOT);
        if (c.contains("beach")) return "beaches";
        if (c.contains("historic") || c.contains("museum") || c.contains("gallery") || c.contains("artwork")) return "heritage";
        if (c.contains("park") || c.contains("zoo")) return "wildlife";
        return "offbeat";
    }

    private String imageForCategory(String category) {
        return switch (category) {
            case "heritage" -> "https://images.unsplash.com/photo-1599661046289-e31897846e41?q=80&w=1200";
            case "hills" -> "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?q=80&w=1200";
            case "beaches" -> "https://images.unsplash.com/photo-1590123047424-1a74a2058afe?q=80&w=1200";
            case "spiritual" -> "https://images.unsplash.com/photo-1561361058-c24cecae35ca?q=80&w=1200";
            case "wildlife" -> "https://images.unsplash.com/photo-1602491453631-e2a5ad90a131?q=80&w=1200";
            default -> "https://images.unsplash.com/photo-1626621331169-5f34be280831?q=80&w=1200";
        };
    }

    private DestinationDto toDestinationDto(Destination d) {
        DestinationDto dto = new DestinationDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setState(d.getState());
        dto.setCategory(d.getCategory());
        dto.setTagline(d.getTagline());
        dto.setDescription(d.getDescription());
        dto.setRating(d.getRating());
        dto.setReviewCount(d.getReviewCount());
        dto.setPriceFrom(d.getPriceFrom());
        dto.setImage(d.getImage());
        dto.setBestSeason(d.getBestSeason());
        dto.setLat(d.getLat());
        dto.setLng(d.getLng());
        dto.setFeatured(d.getFeatured());
        return dto;
    }

    private HotelDto toHotelDto(Hotel h) {
        HotelDto dto = new HotelDto();
        dto.setId(h.getId());
        dto.setDestinationId(h.getDestination().getId());
        dto.setName(h.getName());
        dto.setStars(h.getStars());
        dto.setPricePerNight(h.getPricePerNight());
        dto.setRating(h.getRating());
        dto.setReviewCount(h.getReviewCount());
        dto.setImage(h.getImage());
        dto.setAmenities(h.getAmenities());
        return dto;
    }

    // ---- helpers ----

    private void dedupeByName(List<RawNode> nodes) {
        Set<String> seen = new HashSet<>();
        nodes.removeIf(n -> !seen.add(n.name.trim().toLowerCase(Locale.ROOT)));
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String prettify(String raw) {
        if (raw == null || raw.isBlank()) return "Place of interest";
        String spaced = raw.replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private static final class RawNode {
        final String name;
        final String category;
        final double lat;
        final double lon;

        RawNode(String name, String category, double lat, double lon) {
            this.name = name;
            this.category = category;
            this.lat = lat;
            this.lon = lon;
        }
    }
}
