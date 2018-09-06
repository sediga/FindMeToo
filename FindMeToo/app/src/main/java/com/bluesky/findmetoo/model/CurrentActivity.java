package com.bluesky.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class CurrentActivity {
    private String DeviceId;
    @SerializedName("CurrentActivity")
    private String activity;
    @SerializedName("Lattitude")
    private double latitude;
    @SerializedName("Longitude")
    private double longitude;

}
