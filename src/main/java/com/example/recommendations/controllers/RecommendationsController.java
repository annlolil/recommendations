package com.example.recommendations.controllers;

import com.example.recommendations.dtos.RecommendationDto;
import com.example.recommendations.services.RecommendationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationsController {

    private final RecommendationsService recommendationsService;

    @Autowired
    public RecommendationsController(RecommendationsService recommendationsService) {
        this.recommendationsService = recommendationsService;
    }

    // Keep endpoint for testing
    @GetMapping("/gettopgenres")
    public ResponseEntity<List<String>> calculateTopGenres(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(recommendationsService.calculateTopGenres(jwt), HttpStatus.OK);
    }

    // Keep endpoint for testing
    @GetMapping("/recommendationids")
    public ResponseEntity<List<Long>> calculateRecommendationIds(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(recommendationsService.getRecommendations(jwt), HttpStatus.OK);
    }

    // Main endpoint of microservice
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDto>> calculateRecommendations(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(recommendationsService.formatRecommendations(jwt), HttpStatus.OK);
    }
}
