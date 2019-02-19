package com.puurva.findmetoo.ServiceInterfaces;

import com.google.gson.annotations.SerializedName;
import com.puurva.findmetoo.model.NotificationTocken;
import com.puurva.findmetoo.model.NotificationTocken.NotificationType;

public class NotificationRequestModel {

    public NotificationRequestModel(String deviceID, NotificationType notificationType, String activityId)
    {
        this.DeviceID = deviceID;
        this.RequestNotificationType = notificationType;
        this.ActivityId = activityId;
    }

    @SerializedName("DeviceId")
    public String DeviceID;
    @SerializedName("RequestNotificationType")
    public NotificationType RequestNotificationType;
    @SerializedName("ActivityId")
    public String ActivityId;
}
