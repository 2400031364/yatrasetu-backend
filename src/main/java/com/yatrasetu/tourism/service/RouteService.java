package com.yatrasetu.tourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatrasetu.tourism.dto.RoutePlanResponse;
import com.yatrasetu.tourism.dto.RoutePointDto;
import com.yatrasetu.tourism.dto.RouteStopDto;
import com.yatrasetu.tourism.exception.BadRequestException;
import com.yatrasetu.tourism.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * Plans a real, driveable route between two places the user types in (e.g.
 * "Jaipur" -> "Udaipur") and surfaces the notable tourist places that sit
 * along the way, so a traveller can see what's worth a stop without a
 * detour. Everything here is live, free, keyless OpenStreetMap data:
 *
 *   1. Nominatim — turns "Jaipur" / "Udaipur" into coordinates.
 *   2. OSRM      — the actual shortest drivable road route between them
 *                   (real-time routing engine, not a straight line).
 *   3. Overpass  — named attractions within a corridor around that route.
 */
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OSRM_URL = "https://router.project-osrm.org/route/v1/driving/";

    // Several independent public Overpass mirrors — if the primary one is
    // down or rate-limiting us, we fail over to the next instead of
    // breaking the whole route plan.
    private static final List<String> OVERPASS_MIRRORS = List.of(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://overpass.openstreetmap.ru/api/interpreter"
    );

    // How wide a corridor around the route we scan for places.
    private static final int CORRIDOR_RADIUS_METERS = 12_000;
    // Cap how many stops we ever return, so the map/list stays readable.
    private static final int MAX_STOPS = 18;

    private final ExternalApiClient apiClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public RouteService(ExternalApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private static final Set<String> PLACE_TOURISM_VALUES = Set.of(
            "attraction", "museum", "viewpoint", "zoo", "theme_park", "gallery", "artwork", "aquarium"
    );

    public RoutePlanResponse planRoute(String sourceQuery, String destinationQuery) {
        if (sourceQuery == null || sourceQuery.isBlank() || destinationQuery == null || destinationQuery.isBlank()) {
            throw new BadRequestException("Enter both a source and a destination to plan a route.");
        }

        GeoResult source = geocode(sourceQuery.trim());
        GeoResult destination = geocode(destinationQuery.trim());

        OsrmResult osrm = fetchRoute(source, destination);

        List<SamplePoint> samples = buildSamplePoints(osrm.coordinates, osrm.distanceMeters / 1000.0);
        JsonNode elements = queryOverpassAlongRoute(samples);
        List<RouteStopDto> stops = buildStops(elements, samples);

        RoutePointDto sourceDto = new RoutePointDto(source.label, source.lat, source.lon);
        RoutePointDto destinationDto = new RoutePointDto(destination.label, destination.lat, destination.lon);

        List<double[]> geometry = new ArrayList<>();
        for (double[] lonLat : osrm.coordinates) {
            geometry.add(new double[]{lonLat[1], lonLat[0]}); // -> [lat, lng] for the map
        }

        return new RoutePlanResponse(
                sourceDto,
                destinationDto,
                round1(osrm.distanceMeters / 1000.0),
                round1(osrm.durationSeconds / 60.0),
                geometry,
                stops
        );
    }

    // ---- Nominatim geocoding ----

    private GeoResult geocode(String place) {
        String query = URLEncoder.encode(place, StandardCharsets.UTF_8);
        URI uri = URI.create(NOMINATIM_URL + "?q=" + query + "&format=json&limit=1");

        try {
            HttpResponse<String> response = apiClient.send(
                    ExternalApiClient.get(uri, Duration.ofSeconds(12)), "Geocoding \"" + place + "\"");
            JsonNode results = mapper.readTree(response.body());

            if (!results.isArray() || results.isEmpty()) {
                throw new ResourceNotFoundException("Couldn't find \"" + place + "\". Check the spelling and try again.");
            }

            JsonNode first = results.get(0);
            String label = first.path("display_name").asText(place);
            // Keep the label short — the first one or two comma segments read
            // far better on a card than the full Nominatim address string.
            String[] parts = label.split(",");
            String shortLabel = parts.length > 1 ? (parts[0].trim() + ", " + parts[1].trim()) : parts[0].trim();

            return new GeoResult(shortLabel, first.get("lat").asDouble(), first.get("lon").asDouble());

        } catch (ExternalApiClient.ExternalApiException e) {
            log.error("Nominatim geocoding failed for \"{}\": {}", place, e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error parsing geocoding response for \"{}\"", place, e);
            throw new BadRequestException("Live routing is temporarily unavailable. Please try again in a moment.");
        }
    }

    // ---- OSRM routing ----

    private OsrmResult fetchRoute(GeoResult source, GeoResult destination) {
        String coords = String.format(Locale.ROOT, "%f,%f;%f,%f",
                source.lon, source.lat, destination.lon, destination.lat);
        URI uri = URI.create(OSRM_URL + coords + "?overview=full&geometries=geojson&steps=false");

        try {
            HttpResponse<String> response = apiClient.send(
                    ExternalApiClient.get(uri, Duration.ofSeconds(15)),
                    "Routing " + source.label + " -> " + destination.label);
            JsonNode body = mapper.readTree(response.body());

            if (!"Ok".equalsIgnoreCase(body.path("code").asText())) {
                throw new BadRequestException("Couldn't find a drivable route between those two places.");
            }

            JsonNode route = body.path("routes").get(0);
            double distanceMeters = route.path("distance").asDouble();
            double durationSeconds = route.path("duration").asDouble();

            JsonNode coordsNode = route.path("geometry").path("coordinates");
            List<double[]> coordinates = new ArrayList<>();
            for (JsonNode c : coordsNode) {
                coordinates.add(new double[]{c.get(0).asDouble(), c.get(1).asDouble()}); // [lon, lat]
            }

            return new OsrmResult(distanceMeters, durationSeconds, coordinates);

        } catch (ExternalApiClient.ExternalApiException e) {
            log.error("OSRM routing failed for {} -> {}: {}", source.label, destination.label, e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing OSRM response for {} -> {}", source.label, destination.label, e);
            throw new BadRequestException("Live routing is temporarily unavailable. Please try again in a moment.");
        }
    }

    // ---- Sampling points along the route to scan for places ----

    private List<SamplePoint> buildSamplePoints(List<double[]> coordinates, double totalDistanceKm) {
        List<SamplePoint> samples = new ArrayList<>();
        if (coordinates.size() < 2) return samples;

        // Cumulative distance (km) at every coordinate in the route.
        double[] cumulative = new double[coordinates.size()];
        for (int i = 1; i < coordinates.size(); i++) {
            double[] a = coordinates.get(i - 1);
            double[] b = coordinates.get(i);
            cumulative[i] = cumulative[i - 1] + haversineKm(a[1], a[0], b[1], b[0]);
        }

        // Roughly one scan point every 60km, bounded to a sane range so we
        // neither miss a short trip nor hammer Overpass on a long one.
        int numSamples = (int) Math.round(totalDistanceKm / 60.0);
        numSamples = Math.max(3, Math.min(numSamples, 12));

        for (int i = 1; i <= numSamples; i++) {
            double targetKm = totalDistanceKm * i / (numSamples + 1.0);
            int idx = nearestIndexForDistance(cumulative, targetKm);
            double[] point = coordinates.get(idx);
            samples.add(new SamplePoint(point[1], point[0], cumulative[idx]));
        }
        return samples;
    }

    private int nearestIndexForDistance(double[] cumulative, double targetKm) {
        int lo = 0, hi = cumulative.length - 1;
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (cumulative[mid] < targetKm) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    // ---- Overpass: one combined query covering every sample point ----

    private JsonNode queryOverpassAlongRoute(List<SamplePoint> samples) {
        if (samples.isEmpty()) return mapper.createArrayNode();

        StringBuilder query = new StringBuilder("[out:json][timeout:40];(");
        for (SamplePoint s : samples) {
            query.append(String.format(Locale.ROOT,
                    "node[\"tourism\"~\"attraction|museum|viewpoint|zoo|theme_park|gallery|artwork|aquarium\"](around:%d,%f,%f);",
                    CORRIDOR_RADIUS_METERS, s.lat, s.lon));
            query.append(String.format(Locale.ROOT,
                    "node[\"historic\"](around:%d,%f,%f);",
                    CORRIDOR_RADIUS_METERS, s.lat, s.lon));
            query.append(String.format(Locale.ROOT,
                    "node[\"natural\"=\"beach\"](around:%d,%f,%f);",
                    CORRIDOR_RADIUS_METERS, s.lat, s.lon));
            query.append(String.format(Locale.ROOT,
                    "node[\"leisure\"=\"park\"](around:%d,%f,%f);",
                    CORRIDOR_RADIUS_METERS, s.lat, s.lon));
        }
        query.append(");out body ").append(300).append(";");
        String body = "data=" + URLEncoder.encode(query.toString(), StandardCharsets.UTF_8);

        try {
            HttpResponse<String> response = apiClient.sendWithFallback(
                    OVERPASS_MIRRORS,
                    baseUrl -> HttpRequest.newBuilder(URI.create(baseUrl))
                            .header("User-Agent", "YatraSetu-CollegeProject/1.0 (student demo)")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .timeout(Duration.ofSeconds(45))
                            .POST(HttpRequest.BodyPublishers.ofString(body)),
                    "Overpass lookup along route");
            return mapper.readTree(response.body()).path("elements");
        } catch (Exception e) {
            log.warn("Overpass lookup along route failed on every mirror — returning route without stops: {}",
                    e.getMessage());
            // Places along the route are a bonus, not the core answer — if
            // every Overpass mirror hiccups, still return the route itself
            // instead of failing the whole request.
            return mapper.createArrayNode();
        }
    }

    private List<RouteStopDto> buildStops(JsonNode elements, List<SamplePoint> samples) {
        Map<String, RouteStopDto> byName = new LinkedHashMap<>();

        for (JsonNode el : elements) {
            JsonNode tags = el.path("tags");
            if (!tags.has("name")) continue;

            double lat = el.has("lat") ? el.get("lat").asDouble() : el.path("center").path("lat").asDouble();
            double lon = el.has("lon") ? el.get("lon").asDouble() : el.path("center").path("lon").asDouble();
            if (lat == 0 && lon == 0) continue;

            String name = tags.get("name").asText();
            String key = name.trim().toLowerCase(Locale.ROOT);
            if (byName.containsKey(key)) continue;

            String tourism = tags.path("tourism").asText("");
            boolean isHistoric = tags.has("historic");
            boolean isBeach = "beach".equals(tags.path("natural").asText(""));
            boolean isPark = "park".equals(tags.path("leisure").asText(""));

            String category = isHistoric ? "Historic site"
                    : isBeach ? "Beach"
                    : isPark ? "Park"
                    : PLACE_TOURISM_VALUES.contains(tourism) ? prettify(tourism)
                    : null;
            if (category == null) continue;

            // Approximate how far into the journey this stop sits by
            // matching it to the closest sample point we searched around.
            double bestDist = Double.MAX_VALUE;
            double distanceFromStart = 0;
            for (SamplePoint s : samples) {
                double d = haversineKm(s.lat, s.lon, lat, lon);
                if (d < bestDist) {
                    bestDist = d;
                    distanceFromStart = s.cumulativeKm;
                }
            }

            byName.put(key, new RouteStopDto(name, category, lat, lon, round1(distanceFromStart)));
        }

        List<RouteStopDto> stops = new ArrayList<>(byName.values());
        stops.sort(Comparator.comparingDouble(RouteStopDto::getDistanceFromStartKm));
        if (stops.size() > MAX_STOPS) {
            stops = stops.subList(0, MAX_STOPS);
        }
        return stops;
    }

    // ---- helpers ----

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

    private static final class GeoResult {
        final String label;
        final double lat;
        final double lon;
        GeoResult(String label, double lat, double lon) { this.label = label; this.lat = lat; this.lon = lon; }
    }

    private static final class OsrmResult {
        final double distanceMeters;
        final double durationSeconds;
        final List<double[]> coordinates; // [lon, lat]
        OsrmResult(double distanceMeters, double durationSeconds, List<double[]> coordinates) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.coordinates = coordinates;
        }
    }

    private static final class SamplePoint {
        final double lat;
        final double lon;
        final double cumulativeKm;
        SamplePoint(double lat, double lon, double cumulativeKm) {
            this.lat = lat; this.lon = lon; this.cumulativeKm = cumulativeKm;
        }
    }
}
