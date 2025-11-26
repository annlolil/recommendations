package com.example.recommendations;

import com.example.recommendations.clients.MediaHandlingClient;
import com.example.recommendations.clients.MediaPlayerClient;
import com.example.recommendations.dtos.RecommendationDto;
import com.example.recommendations.services.RecommendationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for the RecommendationsService logic,
 * this unit test only tests "happy routes" currently.
 */
public class RecommendationsServiceTest {

    @Mock private MediaHandlingClient mediaHandlingClient;
    @Mock private MediaPlayerClient mediaPlayerClient;
    @Mock private LoadBalancerClient loadBalancerClient;
    @Mock private RestClient.Builder restClientBuilder;
    @Mock private Jwt jwt;

    RecommendationsService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(jwt.getTokenValue()).thenReturn("dummy-token");

        service = new RecommendationsService(
                mediaHandlingClient,
                mediaPlayerClient,
                restClientBuilder,
                loadBalancerClient
        );
    }

    // getRecommendations()
    @Test
    void testGetRecommendations_basicFlow() {
        // spy = real object where you can override some methods unlike mock which is completely fake,
        // bad practice but unfortunately necessary because our service has too many responsibilities
        RecommendationsService spyService = spy(service);

        // fetchPlayCountByMediaIds()
        Map<Long, Long> playCounts = Map.of(
                10L, 50L,
                20L, 30L,
                30L, 10L
        );
        doReturn(playCounts).when(spyService).fetchPlayCountByMediaIds(jwt);

        // mock methods
        when(mediaPlayerClient.getAllPlayedMedia(jwt)).thenReturn(List.of(1L, 2L));

        when(mediaHandlingClient.getGenreIdByMediaId(10L, jwt)).thenReturn(100L);
        when(mediaHandlingClient.getGenreIdByMediaId(20L, jwt)).thenReturn(200L);
        when(mediaHandlingClient.getGenreIdByMediaId(30L, jwt)).thenReturn(300L);

        when(mediaHandlingClient.getMediaIdsByGenreId(100L, jwt)).thenReturn(List.of(101L, 102L));
        when(mediaHandlingClient.getMediaIdsByGenreId(200L, jwt)).thenReturn(List.of(201L, 202L));
        when(mediaHandlingClient.getMediaIdsByGenreId(300L, jwt)).thenReturn(List.of(301L));

        when(mediaHandlingClient.getAllGenreIds(jwt))
                .thenReturn(List.of(100L, 200L, 300L, 400L));

        when(mediaHandlingClient.getMediaIdsByGenreId(400L, jwt))
                .thenReturn(List.of(401L));

        when(mediaHandlingClient.getAllMediaIds(jwt))
                .thenReturn(List.of(101L, 102L, 201L, 202L, 301L, 401L));

        List<Long> result = spyService.getRecommendations(jwt);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 10);
    }

    // formatRecommendations()
    @Test
    void testFormatRecommendations() {
        RecommendationsService spyService = spy(service);
        doReturn(List.of(10L, 20L)).when(spyService).getRecommendations(jwt);

        when(mediaHandlingClient.getMediaByMediaId(10L, jwt))
                .thenReturn("{\"id\":10,\"name\":\"Track A\"}");
        when(mediaHandlingClient.getMediaByMediaId(20L, jwt))
                .thenReturn("{\"id\":20,\"name\":\"Track B\"}");

        List<RecommendationDto> output = spyService.formatRecommendations(jwt);

        assertEquals(2, output.size());
        assertEquals(10L, output.get(0).getId());
        assertEquals("Track A", output.get(0).getName());
        assertEquals(20L, output.get(1).getId());
    }

    // calculateTopGenres()
    @Test
    void testCalculateTopGenres() {
        RecommendationsService spyService = spy(service);

        Map<Long, Long> playCounts = Map.of(
                10L, 100L,
                20L, 50L
        );
        Map<Long, List<String>> genreMap = Map.of(
                10L, List.of("Rock", "Indie"),
                20L, List.of("Pop")
        );

        doReturn(playCounts).when(spyService).fetchPlayCountByMediaIds(jwt);
        doReturn(genreMap).when(spyService).fetchGenresByMediaIds(playCounts.keySet(), jwt);

        List<String> result = spyService.calculateTopGenres(jwt);

        assertEquals(3, result.size());
        assertTrue(result.contains("Rock"));
        assertTrue(result.contains("Indie"));
        assertTrue(result.contains("Pop"));
    }
}

