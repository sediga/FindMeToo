package com.puurva.findmetoo.ServiceInterfaces;

import com.google.gson.annotations.SerializedName;

public class DeviceModel {

    public DeviceModel(String deviceID, String emailID, String softwareVersion, String notificationTocken)
    {
        this.DeviceID = deviceID;
        this.EmailID = emailID;
        this.SoftwareVersion = softwareVersion;
        this.NotificationToken = notificationTocken;
    }

    @SerializedName("DeviceID")
    public String DeviceID;
    @SerializedName("EmailID")
    public String EmailID;
    @SerializedName("SoftwareVersion")
    public String SoftwareVersion;
    @SerializedName("NotificationToken")
    public String NotificationToken;
}
