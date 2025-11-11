package com.example.recommendations.services;

import com.example.recommendations.dtos.MostPlayedMediaDto;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RecommendationsService implements RecommendationsInterface{

    private final RestTemplate restTemplate;

    @Autowired
    public RecommendationsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Fetches most played media id:s from media-player service
    @Override
    public List<Long> getMostPlayedMediaIds() {

        String url = "http://media-player/api/v1/mediaplayer/mostplayed"; // KOLLA UPP!!

        // add entity for requestentity later to use keycloak authorization..
        /*HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);*/

        ResponseEntity<List<MostPlayedMediaDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<MostPlayedMediaDto>>() {}
        );

        List<MostPlayedMediaDto> mostPlayedMedias = response.getBody();

        if(mostPlayedMedias == null || mostPlayedMedias.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> mediaIds = mostPlayedMedias.stream().map(MostPlayedMediaDto::getMediaId).toList();
        System.out.println("Response " + response);
        System.out.println("Body " + response.getBody());

        return mediaIds;
    }
}
