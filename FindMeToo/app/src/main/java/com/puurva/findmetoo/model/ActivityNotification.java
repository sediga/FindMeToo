package com.puurva.findmetoo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;

public class ActivityNotification implements Parcelable {
    public String DeviceId;
    public  String ActivityId;
    public RequestStatus ActivityRequestStatus;
    public NotificationType ActivityNotiicationType;

    public ActivityNotification(Parcel in) {
        DeviceId = in.readString();
        ActivityId = in.readString();
        ActivityRequestStatus = RequestStatus.valueOf(in.readString());
        ActivityNotiicationType = NotificationType.valueOf(in.readString());
    }

    public ActivityNotification(String deviceId, String activityId, RequestStatus requestStatus, NotificationType notificationType) {
        DeviceId = deviceId;
        ActivityId = activityId;
        ActivityRequestStatus = requestStatus;
        ActivityNotiicationType = notificationType;
    }

    public static final Creator<ActivityNotification> CREATOR = new Creator<ActivityNotification>() {
        @Override
        public ActivityNotification createFromParcel(Parcel in) {
            return new ActivityNotification(in);
        }

        @Override
        public ActivityNotification[] newArray(int size) {
            return new ActivityNotification[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(DeviceId);
        parcel.writeString(ActivityId);
        parcel.writeString(String.valueOf(ActivityRequestStatus));
        parcel.writeString(String.valueOf(ActivityNotiicationType));
    }
}
