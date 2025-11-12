package com.example.recommendations.dtos;

public class PlayedMediaDto {

    private Long mediaId;
    private Long playCount;

    public PlayedMediaDto() {}

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public Long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Long playCount) {
        this.playCount = playCount;
    }
}
