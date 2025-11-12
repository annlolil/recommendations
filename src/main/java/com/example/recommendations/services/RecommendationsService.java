package com.example.recommendations.services;

import com.example.recommendations.dtos.PlayedMediaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationsService implements RecommendationsInterface{

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancer;

    @Autowired
    public RecommendationsService(RestClient.Builder restClientBuilder,LoadBalancerClient loadBalancer) {
        this.restClient = restClientBuilder.build();
        this.loadBalancer = loadBalancer;
    }

    @Override
    public List<String> getRecommendations() { // Working progress...
        return List.of();
    }

    // Fetches play count for each media id from media-player service
    public Map<Long, Long> fetchPlayCountByMediaIds() {

        ServiceInstance serviceInstance = loadBalancer.choose("media-player");
        if (serviceInstance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The service is not available");
        }

        try {
            List<PlayedMediaDto> playedMedias = restClient.get()
                    .uri(serviceInstance.getUri() + "/api/v1/mediaplayer/getmostplayed")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PlayedMediaDto>>() {
                    });

            if (playedMedias == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Received null response from media-player service");
            }
            return playedMedias.stream()
                    .collect(Collectors.toMap(PlayedMediaDto::getMediaId, PlayedMediaDto::getPlayCount));

        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
            } else {
                throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()),
                        "Something went wrong when fetching media id:s: " + e.getResponseBodyAsString());
            }
        }
    }

    // Fetches genres by the media id:s that has been played the most
    public Map<Long, List<String>> fetchGenresByMediaIds(Set<Long> mediaIds) {

        if (mediaIds == null || mediaIds.isEmpty()) {
            return Collections.emptyMap();
        }

        ServiceInstance serviceInstance = loadBalancer.choose("media-handling");
        if (serviceInstance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The service is not available");
        }

        try {
            String idsParam = mediaIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String url = serviceInstance.getUri() + "/api/v1/mediahandling/genresbymediaids?mediaIds=" + idsParam;

            Map<Long, List<String>> genresByMediaIds = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<Long, List<String>>>() {});

            if (genresByMediaIds == null) {
                return Collections.emptyMap();
            }

            return genresByMediaIds;

        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()),
                    "Error fetching genres: " + e.getResponseBodyAsString());
        }
    }

    // Gets a list of top 3 genres based on what genres a user has played the most
    public List<String> calculateTopGenres() {

        Map<Long, Long> playCountsByMediaIds = fetchPlayCountByMediaIds();

        Map<Long, List<String>> genresByMediaIds = fetchGenresByMediaIds(playCountsByMediaIds.keySet());

        Map<String, Long> playCountPerGenre = new HashMap<>();
        for(Map.Entry<Long, List<String>> entry : genresByMediaIds.entrySet()) {
            Long mediaId = entry.getKey();
            List<String> genres = entry.getValue();
            Long playCount = playCountsByMediaIds.get(mediaId);

            for(String genre : genres) {
                playCountPerGenre.put(genre, playCount);
            }
        }

        List<String> topGenres = playCountPerGenre.entrySet().stream().sorted(
                Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(Map.Entry::getKey).toList();

        return topGenres;
    }

}
