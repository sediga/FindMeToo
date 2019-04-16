package com.puurva.findmetoo.ServiceInterfaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CurrentActivity implements Parcelable {

//    public CurrentActivity(String deviceId, String activity, double Lat, double Long, String Description, String activityId){
//        this.DeviceId = deviceId;
//        this.Activity = activity;
//        this.Lat = Lat;
//        this.Long = Long;
//        this.Description = Description;
//        this.ActivityId = activityId;
//    }
//
//    public CurrentActivity(String deviceId, String activity, double Lat, double Long, String Description, String activityId, String activityRequestStatus){
//        this.DeviceId = deviceId;
//        this.Activity = activity;
//        this.Lat = Lat;
//        this.Long = Long;
//        this.Description = Description;
//        this.ActivityId = activityId;
//        this.ActivityRequestStatus = activityRequestStatus;
//    }

    @SerializedName("DeviceID")
    public String DeviceId;
    @SerializedName("Activity")
    public String Activity;
    @SerializedName("Lat")
    public double Lat;
    @SerializedName("Long")
    public double Long;
    @SerializedName("Description")
    public String Description;
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

    public CurrentActivity(Parcel in) {
        DeviceId = in.readString();
        Activity = in.readString();
        Lat = in.readDouble();
        Long = in.readDouble();
        Description = in.readString();
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
        parcel.writeDouble(Lat);
        parcel.writeDouble(Long);
        parcel.writeString(Description);
        parcel.writeString(ImagePath);
        parcel.writeString(ActivityId);
        parcel.writeString(ActivityType);
        parcel.writeString(ActivityStartTime);
        parcel.writeString(ActivityEndTime);
        parcel.writeString(ActivityRequestStatus);
        parcel.writeFloat(ProfileRating);
    }
}
