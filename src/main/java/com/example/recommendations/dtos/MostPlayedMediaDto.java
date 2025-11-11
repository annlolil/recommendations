package com.example.recommendations.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MostPlayedMediaDto {

    private Long mediaId;

    public MostPlayedMediaDto() {}

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }
}
