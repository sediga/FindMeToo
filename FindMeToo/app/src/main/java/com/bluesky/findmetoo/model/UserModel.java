package com.bluesky.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {

    @SerializedName("id")
    private int id;
    @SerializedName("firstName")
    private String first_name;
    @SerializedName("lastName")
    private String last_name;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;

    public UserModel(int id, String first_name, String last_name, double latitude, double longitude) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
