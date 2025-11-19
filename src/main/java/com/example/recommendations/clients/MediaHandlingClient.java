package com.example.recommendations.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Component
public class MediaHandlingClient {

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancer;

    @Autowired
    public MediaHandlingClient(RestClient.Builder builder, LoadBalancerClient loadBalancer) {
        this.restClient = builder.build();
        this.loadBalancer = loadBalancer;
    }

    // Base method used for accessing the microservice
    private String resolveBaseUrl() {
        ServiceInstance instance = loadBalancer.choose("media-handling");
        if (instance == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "media-handling service is not available"
            );
        }
        return instance.getUri().toString();
    }

    // Get genre ID from media ID
    public Long getGenreIdByMediaId(Long mediaID) {
        return restClient.get()
                .uri(resolveBaseUrl() + "/api/v1/mediahandling/genreidbymediaid/{id}", mediaID)
                .retrieve()
                .body(Long.class);
    }

    // Get all media IDs belonging to certain genre
    public List<Long> getMediaIdsByGenreId(Long genreId) {
        List<Map<String, Object>> mediaList = restClient.get()
                .uri(resolveBaseUrl() + "/api/v1/mediahandling/mediabygenre/{id}", genreId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (mediaList == null) {
            return List.of();
        }

        return mediaList.stream()
                .map(entry -> ((Number) entry.get("id")).longValue())
                .toList();
    }

    // Get all existing media IDs
    public List<Long> getAllMediaIds() {
        List<Map<String, Object>> mediaList = restClient.get()
                .uri(resolveBaseUrl() + "/api/v1/mediahandling/media")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (mediaList == null) {
            return List.of();
        }

        return mediaList.stream()
                .map(entry -> ((Number) entry.get("id")).longValue())
                .toList();
    }

    // Get all existing genre IDs
    public List<Long> getAllGenreIds() {
        List<Map<String, Object>> genreList = restClient.get()
                .uri(resolveBaseUrl() + "/api/v1/mediahandling/genres")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (genreList == null) {
            return List.of();
        }

        return genreList.stream()
                .map(entry -> ((Number) entry.get("genreId")).longValue())
                .toList();
    }

    // Get complete media from media ID
    public String getMediaByMediaId(Long mediaId) {
        String media = restClient.get()
                .uri(resolveBaseUrl() + "/api/v1/mediahandling/media/{id}", mediaId)
                .retrieve()
                .body(String.class);

        if (media == null) {
            return "No media found";
        }

        return media;
    }
}
