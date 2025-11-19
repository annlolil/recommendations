package com.example.recommendations.services;

import com.example.recommendations.dtos.RecommendationDto;

import java.util.List;

public interface RecommendationsInterface {

    List<Long> getRecommendations();
    List<RecommendationDto> formatRecommendations();
}
