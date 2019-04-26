package com.puurva.findmetoo.ServiceInterfaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.puurva.findmetoo.Enums.MessageStatuses;

public class NotificationDetails implements Parcelable {
    public String NotificationId;
    public String DeviceId;
    public String ActivityId;
    public String NotificationText;
    public MessageStatuses MessageStatus;
    public String  CreatedOn;
    public String  UpdatedOn;
    public Object MessageObject;
    public  boolean Dismissed;
    public String MessageObjectType;

    public NotificationDetails(Parcel in) {
        NotificationId = in.readString();
        DeviceId = in.readString();
        ActivityId = in.readString();
        NotificationText = in.readString();
        MessageStatus = MessageStatuses.valueOf(in.readString());
        CreatedOn =in.readString();
        UpdatedOn = in.readString();
//        MessageObject = in.readParcelable(NotificationRequestModel.class.getClassLoader());
    }

    public NotificationDetails(String notificationId, String deviceId, String activityId, String notificationText, MessageStatuses messageStatus,
                               String createdOn, String updatedOn, NotificationRequestModel notificationRequestModel, boolean dismissed) {
        NotificationId = notificationId;
        DeviceId = deviceId;
        ActivityId = activityId;
        NotificationText = notificationText;
        MessageStatus = messageStatus;
        CreatedOn = createdOn;
        UpdatedOn = updatedOn;
        MessageObject = notificationRequestModel;
        Dismissed = dismissed;
    }

    public static final Creator<NotificationDetails> CREATOR = new Creator<NotificationDetails>() {
        @Override
        public NotificationDetails createFromParcel(Parcel in) {
            return new NotificationDetails(in);
        }

        @Override
        public NotificationDetails[] newArray(int size) {
            return new NotificationDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(NotificationId);
        parcel.writeString(DeviceId);
        parcel.writeString(ActivityId);
        parcel.writeString(NotificationText);
        parcel.writeString(MessageStatus.toString());
        parcel.writeString(CreatedOn);
        parcel.writeString(UpdatedOn);
//        parcel.writeParcelable(MessageObject, i);
    }
}
