package com.sszabo.life_tok.model;

/**
 * The post video object that will be used to display in exoplayer
 */
public class MediaObject {

    private String title;
    private String videoUrl;
    private String thumbnail;
    private String description;

    public MediaObject(String title, String videoUrl, String thumbnail, String description) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.thumbnail = thumbnail;
        this.description = description;
    }

    public MediaObject() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
