package com.sszabo.life_tok.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public class Event implements Serializable {

    private String id;
    private String userId;
    private String name;
    private String description;
    private String mediaUrl;
    // TODO use picture/thumbnail only when not video. Otherwise save thumbnail pic for video
    private String thumbnailUrl;
    private boolean isPicture;

    /**
     * Public (1) or private (0) event
     */
    private int eventType;

    private GeoPoint geoPoint;
    private String locationName;
    private Timestamp timestamp;

    public Event(String id, String userId, String name, String description, String mediaUrl, String thumbnailUrl,
                 boolean isPicture, int eventType, GeoPoint geoPoint, String locationName, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.isPicture = isPicture;
        this.eventType = eventType;
        this.geoPoint = geoPoint;
        this.locationName = locationName;
        this.timestamp = timestamp;
    }

    public Event(String name, String description, String mediaUrl, String thumbnailUrl, boolean isPicture,
                 int eventType, GeoPoint geoPoint, String locationName, Timestamp timestamp) {
        this.name = name;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.isPicture = isPicture;
        this.eventType = eventType;
        this.geoPoint = geoPoint;
        this.locationName = locationName;
        this.timestamp = timestamp;
    }

    /**
     * Empty constructor
     */
    public Event() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPicture() {
        return isPicture;
    }

    public void setPicture(boolean picture) {
        isPicture = picture;
    }

    /**
     * Searches an event for name, description, or location name.
     *
     * @param query to search for
     * @return true if event contains query, false otherwise
     */
    public boolean contains(String query) {
        if (this.name.toLowerCase().contains(query)) {
            return true;
        }

        if (this.description.toLowerCase().contains(query)) {
            return true;
        }

        return this.locationName.toLowerCase().contains(query);
    }
}
