package com.puurva.findmetoo.model;

import com.google.gson.annotations.SerializedName;
import com.puurva.findmetoo.Enums.ActivityTypes;

import java.util.Date;

public class CurrentActivity {

    public CurrentActivity(String deviceId, String activity, double latitude, double longitude, String description, String activityId){
        this.DeviceId = deviceId;
        this.Activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.ActivityId = activityId;
    }

    @SerializedName("DeviceID")
    public String DeviceId;
    @SerializedName("Activity")
    public String Activity;
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
    @SerializedName("ActivityType")
    public String ActivityType;
    @SerializedName("ActivityStartTime")
    public String ActivityStartTime;
    @SerializedName("ActivityEndTime")
    public String ActivityEndTime;
}
