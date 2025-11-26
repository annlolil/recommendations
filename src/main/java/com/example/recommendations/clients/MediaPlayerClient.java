package com.example.recommendations.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Component
public class MediaPlayerClient {

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancer;
    private final ObjectMapper objectMapper;

    @Autowired
    public MediaPlayerClient(RestClient.Builder builder, LoadBalancerClient loadBalancer, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.loadBalancer = loadBalancer;
        this.objectMapper = objectMapper;
    }

    // Base method used for accessing the microservice
    private String resolveBaseUrl() {
        ServiceInstance instance = loadBalancer.choose("media-player");
        if (instance == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "media-player service is not available"
            );
        }
        return instance.getUri().toString();
    }

    // Helper method for parsing JSON response from media-player and collecting IDs of media
    private List<Long> extractMediaIDs(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            List<Long> ids = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    // Expect field "id"
                    if (node.has("id")) {
                        ids.add(node.get("id").asLong());
                    }
                }
            }
            return ids;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // IDs of user's MOST PLAYED media
    public List<Long> getMostPlayedMedia() {
        try {
            String json = restClient.get()
                    .uri(resolveBaseUrl() + "/api/v1/mediaplayer/mostplayed")
                    .retrieve()
                    .body(String.class);
            return extractMediaIDs(json);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Failed to fetch most played media: " + e.getResponseBodyAsString()
            );
        }
    }

    // IDs of user's ALL PLAYED media
    public List<Long> getAllPlayedMedia(Jwt jwt) {

        String token = jwt.getTokenValue();

        try {
            String json = restClient.get()
                    .uri(resolveBaseUrl() + "/api/v1/mediaplayer/allplayed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(String.class);
            return extractMediaIDs(json);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Failed to fetch all played media: " + e.getResponseBodyAsString()
            );
        }
    }

    // IDs of user's LIKED media
    public List<Long> getLikedMedia() {
        try {
            String json = restClient.get()
                    .uri(resolveBaseUrl() + "/api/v1/mediaplayer/liked")
                    .retrieve()
                    .body(String.class);
            return extractMediaIDs(json);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Failed to fetch liked media: " + e.getResponseBodyAsString()
            );
        }
    }

    // IDs of user's DISLIKED media
    public List<Long> getDislikedMedia() {
        try {
            String json = restClient.get()
                    .uri(resolveBaseUrl() + "/api/v1/mediaplayer/disliked")
                    .retrieve()
                    .body(String.class);
            return extractMediaIDs(json);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Failed to fetch disliked media: " + e.getResponseBodyAsString()
            );
        }
    }
}
