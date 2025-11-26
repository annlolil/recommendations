package com.example.recommendations.services;

import com.example.recommendations.dtos.RecommendationDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface RecommendationsInterface {

    List<Long> getRecommendations(Jwt jwt);
    List<RecommendationDto> formatRecommendations(Jwt jwt);
}
