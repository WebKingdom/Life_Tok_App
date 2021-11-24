package com.sszabo.life_tok.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Event {

    private int id;
    private int userId;
    private String name;
    private String description;

    /**
     * Public (1) or private (0) event
     */
    private int eventType;

    private GeoPoint geoPoint;
    private Timestamp timestamp;

    public Event(int id, int userId, String name, String description, int eventType, GeoPoint geoPoint, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
