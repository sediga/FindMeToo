package com.puurva.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class ActivityModel {

    @SerializedName("id")
    private int id;
    @SerializedName("userid")
    private String userId;
    @SerializedName("activity")
    private String activity;
    @SerializedName("Lat")
    private double Lat;
    @SerializedName("Long")
    private double Long;

    public ActivityModel(int id, String userId, String activity, double Lat, double Long) {
        this.id = id;
        this.userId = userId;
        this.activity = activity;
        this.Lat = Lat;
        this.Long = Long;
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

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        this.Lat = lat;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        this.Long = aLong;
    }
}
