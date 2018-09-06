package com.bluesky.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class ActivityModel {

    @SerializedName("id")
    private int id;
    @SerializedName("userid")
    private String userId;
    @SerializedName("activity")
    private String activity;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;

    public ActivityModel(int id, String userId, String activity, double latitude, double longitude) {
        this.id = id;
        this.userId = userId;
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
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
