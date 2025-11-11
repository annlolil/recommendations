package com.example.recommendations.services;

import com.example.recommendations.dtos.MostPlayedMediaDto;

import java.util.List;
import java.util.Map;

public interface RecommendationsInterface {

    List<Long> getMostPlayedMediaIds();

//    Map<Long, List<String>> getGenresByMediaIds(List<Long> mediaIds);
}
