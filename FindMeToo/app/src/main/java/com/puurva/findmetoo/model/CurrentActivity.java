package com.puurva.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class CurrentActivity {

    public CurrentActivity(String deviceId, String activity, double latitude, double longitude, String description, String activityId){
        this.DeviceId = deviceId;
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.ActivityId = activityId;
    }

    @SerializedName("DeviceID")
    public String DeviceId;
    @SerializedName("CurrentActivity")
    public String activity;
    @SerializedName("Lat")
    public double latitude;
    @SerializedName("Long")
    public double longitude;
    @SerializedName("Description")
    public String description;
    @SerializedName("ImagePath")
    public String ImagePath;
    @SerializedName("ActivityId")
    public String ActivityId;
}
