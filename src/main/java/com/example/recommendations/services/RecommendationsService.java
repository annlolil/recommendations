package com.example.recommendations.services;

import com.example.recommendations.clients.MediaHandlingClient;
import com.example.recommendations.clients.MediaPlayerClient;
import com.example.recommendations.dtos.PlayedMediaDto;
import com.example.recommendations.dtos.RecommendationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationsService implements RecommendationsInterface{

    private final MediaHandlingClient mediaHandlingClient;
    private final MediaPlayerClient mediaPlayerClient;
    private final RestClient restClient;
    private final LoadBalancerClient loadBalancer;

    @Autowired
    public RecommendationsService(MediaHandlingClient mediaHandlingClient,
                                  MediaPlayerClient mediaPlayerClient,
                                  RestClient.Builder restClientBuilder,
                                  LoadBalancerClient loadBalancer) {
        this.mediaHandlingClient = mediaHandlingClient;
        this.mediaPlayerClient = mediaPlayerClient;
        this.restClient = restClientBuilder.build();
        this.loadBalancer = loadBalancer;
    }

    @Override
    public List<Long> getRecommendations(Jwt jwt) {
        // Get ID list of user's played media
        List<Long> streamedMedia = mediaPlayerClient.getAllPlayedMedia(jwt);

        // Get streaming history by songId:"playCount"
        Map<Long, Long> playCounts = fetchPlayCountByMediaIds(jwt);

        // Calculate top genres (max 3)
        Set<Long> topGenres = new LinkedHashSet<>();
        playCounts.entrySet().stream()
                // Sort by playCount, DESC
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    if (topGenres.size() < 3) {
                        Long genreId = mediaHandlingClient.getGenreIdByMediaId(entry.getKey(), jwt);
                        topGenres.add(genreId);
                    }
                });

        // To return later
        List<Long> recommendations = new ArrayList<>();

        // !!! WIP, INCOMPLETE LOGIC BELOW !!!
        // Needs to parse through all media AGAIN if recommendations < 10

        // Get 8 media based on top genres
        List<Long> topGenreCandidates = new ArrayList<>();
        for (Long genreId : topGenres) {
            List<Long> media = mediaHandlingClient.getMediaIdsByGenreId(genreId, jwt);
            media.stream()
                    .filter(id -> !streamedMedia.contains(id))
                    .forEach(topGenreCandidates::add);
        }

        Collections.shuffle(topGenreCandidates);
        int topGenrePickCount = Math.min(8, topGenreCandidates.size());
        recommendations.addAll(topGenreCandidates.subList(0, topGenrePickCount));

        // Get from other genres
        List<Long> allGenres = new ArrayList<>(mediaHandlingClient.getAllGenreIds(jwt));
        allGenres.removeAll(topGenres);

        List<Long> otherGenreCandidates = new ArrayList<>();
        for (Long otherGenre : allGenres) {
            List<Long> media = mediaHandlingClient.getMediaIdsByGenreId(otherGenre, jwt);
            media.stream()
                    .filter(id -> !streamedMedia.contains(id))
                    .filter(id -> !recommendations.contains(id))
                    .forEach(otherGenreCandidates::add);
        }

        Collections.shuffle(otherGenreCandidates);
        int otherPickCount = Math.min(2, otherGenreCandidates.size());
        recommendations.addAll(otherGenreCandidates.subList(0, otherPickCount));

        if (recommendations.size() < 10) {
            List<Long> allMedia = mediaHandlingClient.getAllMediaIds(jwt);

            List<Long> unused = allMedia.stream()
                    .filter(id -> !streamedMedia.contains(id))
                    .filter(id -> !recommendations.contains(id))
                    .toList();

            for (Long id : unused) {
                if (recommendations.size() >= 10) break;
                recommendations.add(id);
            }
        }

        return recommendations;
    }

    @Override
    public List<RecommendationDto> formatRecommendations(Jwt jwt) {
        // Get IDs of recommendations
        List<Long> recommendationIds = getRecommendations(jwt);

        // Store final recommendations in presentable format
        List<RecommendationDto> recommendations = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper(); // create mapper
        mapper.registerModule(new JavaTimeModule()); // make mapper support parsing of LocalDate in media
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // get better date format

        // For each ID, use media-handling for mapping ID into RecommendationDto
        for (Long id : recommendationIds) {
            try {
                String mediaJson = mediaHandlingClient.getMediaByMediaId(id, jwt);

                RecommendationDto dto =
                        mapper.readValue(mediaJson, RecommendationDto.class);

                recommendations.add(dto);

            } catch (Exception e) {
                System.err.println("Failed to parse media ID " + id + ": " + e.getMessage());
            }
        }

        return recommendations;
    }

    // Fetches play count for each media id from media-player service
    public Map<Long, Long> fetchPlayCountByMediaIds(Jwt jwt) {

        String token = jwt.getTokenValue();

        ServiceInstance serviceInstance = loadBalancer.choose("media-player");
        if (serviceInstance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The service is not available");
        }

        try {
            List<PlayedMediaDto> playedMedias = restClient.get()
                    .uri(serviceInstance.getUri() + "/api/v1/mediaplayer/allplayed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    public Map<Long, List<String>> fetchGenresByMediaIds(Set<Long> mediaIds, Jwt jwt) {

        if (mediaIds == null || mediaIds.isEmpty()) {
            return Collections.emptyMap();
        }

        ServiceInstance serviceInstance = loadBalancer.choose("media-handling");
        if (serviceInstance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The service is not available");
        }

        String token = jwt.getTokenValue();

        try {
            String idsParam = mediaIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String url = serviceInstance.getUri() + "/api/v1/mediahandling/genresbymediaids?mediaIds=" + idsParam;

            Map<Long, List<String>> genresByMediaIds = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    public List<String> calculateTopGenres(Jwt jwt) {

        Map<Long, Long> playCountsByMediaIds = fetchPlayCountByMediaIds(jwt);

        Map<Long, List<String>> genresByMediaIds = fetchGenresByMediaIds(playCountsByMediaIds.keySet(), jwt);

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
