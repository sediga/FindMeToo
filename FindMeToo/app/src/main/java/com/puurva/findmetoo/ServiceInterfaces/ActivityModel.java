package com.puurva.findmetoo.ServiceInterfaces;

import com.google.gson.annotations.SerializedName;

public class ActivityModel {

    public ActivityModel(String deviceID, String what, String description, String when, double latitude, double longitude)
    {
        this.DeviceID = deviceID;
        this.What = what;
        this.When = when;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.description = description;
    }

    @SerializedName("DeviceID")
    public String DeviceID;
    @SerializedName("What")
    public String What;
    @SerializedName("When")
    public String When;
    @SerializedName("Lat")
    public double Latitude;
    @SerializedName("Long")
    public double Longitude;
    @SerializedName("description")
    public String description;
}
