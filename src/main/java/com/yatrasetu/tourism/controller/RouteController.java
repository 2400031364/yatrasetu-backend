package com.yatrasetu.tourism.controller;

import com.yatrasetu.tourism.dto.RoutePlanResponse;
import com.yatrasetu.tourism.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * Plans a real, drivable route between two places and returns the
     * shortest live route (distance/duration + map geometry) plus the
     * notable tourist places found along the way.
     * Example: GET /api/routes/plan?source=Jaipur&destination=Udaipur
     */
    @GetMapping("/plan")
    public ResponseEntity<RoutePlanResponse> plan(@RequestParam String source, @RequestParam String destination) {
        return ResponseEntity.ok(routeService.planRoute(source, destination));
    }
}
