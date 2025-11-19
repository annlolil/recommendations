package com.example.recommendations.dtos;

import java.time.LocalDate;
import java.util.List;

/**
 * Used for clean media presentation in the /recommendations endpoint.
 */
public class RecommendationDto {

    private Long id;
    private String name;
    private String url;
    private LocalDate releaseDate;
    private String mediaType;
    private List<String> genres;
    private List<String> artists;
    private List<String> albums;

    public RecommendationDto() { }

    public RecommendationDto(Long id,
                             String name,
                             String url,
                             LocalDate releaseDate,
                             String mediaType,
                             List<String> genres,
                             List<String> artists,
                             List<String> albums) {
        this.name = name;
        this.url = url;
        this.releaseDate = releaseDate;
        this.mediaType = mediaType;
        this.genres = genres;
        this.artists = artists;
        this.albums = albums;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public List<String> getAlbums() {
        return albums;
    }

    public void setAlbums(List<String> albums) {
        this.albums = albums;
    }
}
