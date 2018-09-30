package com.bluesky.findmetoo.ServiceInterfaces;

import com.google.gson.annotations.SerializedName;

public class ActivityModel {

    public ActivityModel(String deviceID, String what, String when)
    {
        this.DeviceID = deviceID;
        this.What = what;
        this.When = when;
    }

    @SerializedName("DeviceID")
    public String DeviceID;
    @SerializedName("What")
    public String What;
    @SerializedName("When")
    public String When;
}
