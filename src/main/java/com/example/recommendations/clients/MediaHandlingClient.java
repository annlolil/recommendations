package com.example.recommendations.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    // Get all genres from media-handling
    public List<String> getAllGenres() {
        return null;
    }

    // Get all media belonging to certain genre
    public List<String> getMediaByGenreID(Long genreId) {
        return null;
    }

    // Get genres from media IDs
    public List<String> getMediasByGenreIDs(Long genreId, String genreName) {
        return null;
    }
}
