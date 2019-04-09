package com.puurva.findmetoo.ServiceInterfaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.puurva.findmetoo.Enums.ActivityTypes;

import java.util.Date;

public class CurrentActivity implements Parcelable {

    public CurrentActivity(String deviceId, String activity, double latitude, double longitude, String description, String activityId){
        this.DeviceId = deviceId;
        this.Activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.ActivityId = activityId;
    }

    public CurrentActivity(String deviceId, String activity, double latitude, double longitude, String description, String activityId, String activityRequestStatus){
        this.DeviceId = deviceId;
        this.Activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.ActivityId = activityId;
        this.ActivityRequestStatus = activityRequestStatus;
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
    @SerializedName("ActivityRequestStatus")
    public String ActivityRequestStatus;
    @SerializedName("ProfileRating")
    public float ProfileRating;

    protected CurrentActivity(Parcel in) {
        DeviceId = in.readString();
        Activity = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        description = in.readString();
        ImagePath = in.readString();
        ActivityId = in.readString();
        ActivityType = in.readString();
        ActivityStartTime = in.readString();
        ActivityEndTime = in.readString();
        ActivityRequestStatus = in.readString();
        ProfileRating = in.readFloat();
    }

    public static final Creator<CurrentActivity> CREATOR = new Creator<CurrentActivity>() {
        @Override
        public CurrentActivity createFromParcel(Parcel in) {
            return new CurrentActivity(in);
        }

        @Override
        public CurrentActivity[] newArray(int size) {
            return new CurrentActivity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(DeviceId);
        parcel.writeString(Activity);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(description);
        parcel.writeString(ImagePath);
        parcel.writeString(ActivityId);
        parcel.writeString(ActivityType);
        parcel.writeString(ActivityStartTime);
        parcel.writeString(ActivityEndTime);
        parcel.writeString(ActivityRequestStatus);
        parcel.writeFloat(ProfileRating);
    }
}
