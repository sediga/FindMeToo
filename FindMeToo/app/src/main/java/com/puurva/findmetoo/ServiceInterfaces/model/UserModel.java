package com.puurva.findmetoo.ServiceInterfaces.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {

    @SerializedName("id")
    private int id;
    @SerializedName("uid")
    private String uid;
    @SerializedName("displayname")
    private String displayname;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("Lat")
    private double latitude;
    @SerializedName("Long")
    private double longitude;

    public UserModel(int id, String first_name, String last_name) {
        this.id = id;
        this.username = first_name;
        this.password = last_name;
//        this.latitude = latitude;
//        this.longitude = longitude;
    }

    public UserModel(String uid, String userName, String displayName) {
        this.username = userName;
        this.uid = uid;
        this.displayname = displayName;
//        this.latitude = latitude;
//        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public double getLatitude() {
//        return latitude;
//    }
//
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//
//    public double getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
}
