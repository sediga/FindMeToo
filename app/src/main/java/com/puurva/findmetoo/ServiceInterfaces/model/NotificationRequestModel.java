package com.puurva.findmetoo.ServiceInterfaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;

public class NotificationRequestModel implements Parcelable {

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

    protected NotificationRequestModel(Parcel in) {
        FromDeviceId = in.readString();
        ToDeviceId = in.readString();
        RequestNotificationType = NotificationType.valueOf(in.readString());
        ActivityId = in.readString();
        RequestNotificationStatus = RequestStatus.valueOf(in.readString());
    }

    public static final Creator<NotificationRequestModel> CREATOR = new Creator<NotificationRequestModel>() {
        @Override
        public NotificationRequestModel createFromParcel(Parcel in) {
            return new NotificationRequestModel(in);
        }

        @Override
        public NotificationRequestModel[] newArray(int size) {
            return new NotificationRequestModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(FromDeviceId);
        parcel.writeString(ToDeviceId);
        parcel.writeString(RequestNotificationType.name());
        parcel.writeString(ActivityId);
        parcel.writeString(RequestNotificationStatus.name());
    }
}
