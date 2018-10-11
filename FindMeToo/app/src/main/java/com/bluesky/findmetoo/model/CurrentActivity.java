package com.bluesky.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class CurrentActivity {

    public CurrentActivity(String deviceId, String activity, double latitude, double longitude, String description){
        this.DeviceId = deviceId;
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    @SerializedName("DeviceID")
    public String DeviceId;
    @SerializedName("CurrentActivity")
    public String activity;
    @SerializedName("Lat")
    public double latitude;
    @SerializedName("Long")
    public double longitude;
    @SerializedName("description")
    public String description;

}
