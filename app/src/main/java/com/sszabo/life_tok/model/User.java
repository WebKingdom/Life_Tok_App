package com.sszabo.life_tok.model;

import java.util.List;

public class User {

    private String id;
    private String firstName;
    private String lastName;

    private String email;
    private String address;
    private String phoneNo;

    private String username;

    private List<String> following;
    private List<String> followers;

    /**
     * Populated constructor
     * @param id
     * @param firstName
     * @param lastName
     * @param username
     * @param email
     * @param address
     * @param phoneNo
     * @param following
     * @param followers
     */
    public User(String id, String firstName, String lastName, String username, String email,
                String address, String phoneNo, List<String> following, List<String> followers) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.address = address;
        this.phoneNo = phoneNo;
        this.following = following;
        this.followers = followers;
    }

    /**
     * Empty constructor
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
}
