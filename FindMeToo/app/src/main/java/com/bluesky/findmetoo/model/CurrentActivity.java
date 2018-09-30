package com.bluesky.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class CurrentActivity {

    public CurrentActivity(String deviceId, String activity, double latitude, double longitude){
        this.DeviceId = deviceId;
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @SerializedName("DeviceID")
    public String DeviceId;
    @SerializedName("CurrentActivity")
    public String activity;
    @SerializedName("Lat")
    public double latitude;
    @SerializedName("Long")
    public double longitude;

}
