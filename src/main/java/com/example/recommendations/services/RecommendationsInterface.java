package com.example.recommendations.services;

import com.example.recommendations.dtos.MostPlayedMediaDto;

import java.util.List;

public interface RecommendationsInterface {

    List<Long> getMostPlayedMediaIds();
}
