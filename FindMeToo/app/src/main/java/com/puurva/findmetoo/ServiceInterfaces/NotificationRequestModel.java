package com.puurva.findmetoo.ServiceInterfaces;

import com.google.gson.annotations.SerializedName;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;

public class NotificationRequestModel {

    public NotificationRequestModel(String fromDeviceID, String toDeviceId, NotificationType notificationType, String activityId, RequestStatus requestStatus)
    {
        this.FromDeviceId = fromDeviceID;
        this.ToDeviceId = toDeviceId;
        this.RequestNotificationType = notificationType;
        this.ActivityId = activityId;
        this.RequestNotificationStatus = requestStatus;
    }

    @SerializedName("FromDeviceId")
    public String FromDeviceId;
    @SerializedName("ToDeviceId")
    public String ToDeviceId;
    @SerializedName("RequestNotificationType")
    public NotificationType RequestNotificationType;
    @SerializedName("ActivityId")
    public String ActivityId;
    @SerializedName("NotificationRequestStatus")
    public RequestStatus RequestNotificationStatus;
}
