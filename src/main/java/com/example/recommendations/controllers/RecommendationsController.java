package com.example.recommendations.controllers;

import com.example.recommendations.dtos.RecommendationDto;
import com.example.recommendations.services.RecommendationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<String>> calculateTopGenres() {
        return new ResponseEntity<>(recommendationsService.calculateTopGenres(), HttpStatus.OK);
    }

    // Keep endpoint for testing
    @GetMapping("/recommendationids")
    public ResponseEntity<List<Long>> calculateRecommendationIds() {
        return new ResponseEntity<>(recommendationsService.getRecommendations(), HttpStatus.OK);
    }

    // Main endpoint of microservice
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDto>> calculateRecommendations() {
        return new ResponseEntity<>(recommendationsService.formatRecommendations(), HttpStatus.OK);
    }
}
