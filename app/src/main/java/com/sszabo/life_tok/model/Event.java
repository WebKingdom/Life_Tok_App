package com.sszabo.life_tok.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Locale;

public class Event {

    private int id;
    private String userId;
    private String name;
    private String description;
    private String mediaUrl;

    /**
     * Public (1) or private (0) event
     */
    private int eventType;

    private GeoPoint geoPoint;
    private String locationName;
    private Timestamp timestamp;

    public Event(String userId, String name, String description, String mediaUrl, int eventType, GeoPoint geoPoint, Timestamp timestamp) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.eventType = eventType;
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
    }

    public Event(String name, String description, String mediaUrl, int eventType, GeoPoint geoPoint, Timestamp timestamp) {
        this.name = name;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.eventType = eventType;
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
    }

    /**
     * Empty constructor
     */
    public Event() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Searches an event for name, description, or location name.
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
