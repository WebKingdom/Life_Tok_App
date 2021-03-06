package com.sszabo.life_tok.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

/**
 * A model class for an event. It contains all the information an event is composed of.
 */
public class Event implements Serializable {

    private String id;
    private String userId;
    private String username;
    private String name;
    private String description;
    private int numLikes;
    private String mediaUrl;

    // TODO use picture/thumbnail for profile picture?
    private String thumbnailUrl;
    private boolean isPicture;

    /**
     * Public (1) or private (0) event
     */
    private int eventType;

    private GeoPoint geoPoint;
    private String locationName;
    private Timestamp timestamp;

    /**
     * Constructor for an event.
     *
     * @param id           of event
     * @param userId       of the user who posted the event
     * @param username     of the user who posted the event
     * @param name         of the user who posted the event
     * @param description  of the event
     * @param likes        number of likes for the event
     * @param mediaUrl     URL location of the media for the event
     * @param thumbnailUrl URL location for the thumbnail of the event
     * @param isPicture    boolean marking it event contains a picture or video
     * @param eventType    type of event (1-public, 0-private)
     * @param geoPoint     location of the event
     * @param locationName name for the location of the event
     * @param timestamp    time the event was posted
     */
    public Event(String id, String userId, String username, String name, String description, int likes, String mediaUrl,
                 String thumbnailUrl, boolean isPicture, int eventType, GeoPoint geoPoint, String locationName,
                 Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.description = description;
        this.numLikes = likes;
        this.mediaUrl = mediaUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.isPicture = isPicture;
        this.eventType = eventType;
        this.geoPoint = geoPoint;
        this.locationName = locationName;
        this.timestamp = timestamp;
    }

    /**
     * Empty event constructor
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int likes) {
        this.numLikes = likes;
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
     * Searches an event for name, description, username, or location.
     *
     * @param query to search for
     * @return true if event contains query, false otherwise
     */
    public boolean searchContains(String query) {
        if (this.name.toLowerCase().contains(query)) {
            return true;
        }

        if (this.description.toLowerCase().contains(query)) {
            return true;
        }

        if (this.username.toLowerCase().contains(query)) {
            return true;
        }

        return this.locationName.toLowerCase().contains(query);
    }
}
