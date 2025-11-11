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

    //Can delete this later on...
    @GetMapping("/mostplayedmediaids") //testing
    public ResponseEntity<List<Long>> mostPlayedMediaIds() {
        return new ResponseEntity<>(recommendationsService.getMostPlayedMediaIds(), HttpStatus.OK);
    }

//    // Used to give recommendations
//    @GetMapping("/getgenresbymediaids")
//    public ResponseEntity<List<String>> genresByMediaIds() {
//        return new ResponseEntity<>(recommendationsService.getGenresByMediaIds(), HttpStatus.OK);
//    }
}
