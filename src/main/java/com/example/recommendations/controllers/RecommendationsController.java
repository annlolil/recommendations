package com.example.recommendations.controllers;

import com.example.recommendations.services.RecommendationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RecommendationsController {

    private final RecommendationsService recommendationsService;

    @Autowired
    public RecommendationsController( RecommendationsService recommendationsService) {
        this.recommendationsService = recommendationsService;
    }

    // Can remove later on, just added this now to test it in postman.
    @GetMapping("/gettopgenres")
    public ResponseEntity<List<String>> calculateTopGenres() {
        return new ResponseEntity<>(recommendationsService.calculateTopGenres(), HttpStatus.OK);
    }

    // Working progress...
    @GetMapping("/recommendation")
    public ResponseEntity<List<String>> recommendation() {
        return new ResponseEntity<>(recommendationsService.getRecommendations(), HttpStatus.OK);
    }
}
