package com.sszabo.life_tok.model;

import java.util.List;

/**
 * A model class for a user. It contains all the information a user is composed of.
 */
public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String username;

    private String email;
    private String address;
    private String phoneNo;

    private String pictureUrl;

    /**
     * List of public event IDs
     */
    private List<String> publicEventIds;

    /**
     * List of following user IDs
     */
    private List<String> following;

    /**
     * List of follower user IDs
     */
    private List<String> followers;

    /**
     * Constructor for a user.
     *
     * @param id             of user
     * @param firstName      of user
     * @param lastName       of user
     * @param username       of user
     * @param email          of user
     * @param address        of user
     * @param phoneNo        of user
     * @param pictureUrl     URL location for the profile picture of the user
     * @param publicEventIds list of public event IDs (strings)
     * @param following      list of following event IDs
     * @param followers      list of follower event IDs
     */
    public User(String id, String firstName, String lastName, String username, String email, String address,
                String phoneNo, String pictureUrl, List<String> publicEventIds, List<String> following,
                List<String> followers) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.address = address;
        this.phoneNo = phoneNo;
        this.pictureUrl = pictureUrl;
        this.publicEventIds = publicEventIds;
        this.following = following;
        this.followers = followers;
    }

    /**
     * Empty user constructor
     */
    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public List<String> getPublicEventIds() {
        return publicEventIds;
    }

    public void setPublicEventIds(List<String> publicEventIds) {
        this.publicEventIds = publicEventIds;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public boolean userEquals(User user) {
        return this.firstName.equals(user.getFirstName()) &&
                this.lastName.equals(user.getLastName()) &&
                this.username.equals(user.getUsername()) &&
                this.email.equals(user.getEmail()) &&
                this.address.equals(user.getAddress()) &&
                this.phoneNo.equals(user.getPhoneNo()) &&
                this.pictureUrl.equals(user.getPictureUrl());
    }
}
