package com.example.recommendations.services;

import com.example.recommendations.dtos.MostPlayedMediaDto;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class RecommendationsService implements RecommendationsInterface{

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancer;

    @Autowired
    public RecommendationsService(RestClient.Builder restClientBuilder,LoadBalancerClient loadBalancer) {
        this.restClient = restClientBuilder.build();
        this.loadBalancer = loadBalancer;
    }

    // Fetches most played media id:s from media-player service
    @Override
    public List<Long> getMostPlayedMediaIds() { // Add string token later when implementing keycloak auth

        List<Long> mediaIds;

        ServiceInstance serviceInstance = loadBalancer.choose("media-player");
        if (serviceInstance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The service is not available");
        }

        try {
            List<MostPlayedMediaDto> mostPlayedMedias = restClient.get()
                    .uri(serviceInstance.getUri() + "/api/v1/mediaplayer/mostplayed")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MostPlayedMediaDto>>() {
                    });

            if (mostPlayedMedias == null || mostPlayedMedias.isEmpty()) {
                return Collections.emptyList();
            }

            mediaIds = mostPlayedMedias.stream().map(MostPlayedMediaDto::getMediaId).toList();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
            } else {
                throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()),
                        "Something went wrong when fetching media: " + e.getResponseBodyAsString());
            }
        }
        return mediaIds;
    }



//    private Map<Long, List<String>> fetchGenresByMediaIds(List<Long> mediaIds) {
//        List<String> genres = new ArrayList<>();
//
//
//
//
//    }
}
