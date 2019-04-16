package com.puurva.findmetoo.ServiceInterfaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ActivityModel implements Parcelable {

    public ActivityModel(String activityID, String deviceID, String what, String description, String when, double latitude, double longitude)
    {
        this.ActivityID = activityID;
        this.DeviceID = deviceID;
        this.What = what;
        this.When = when;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.description = description;
    }

    public ActivityModel(String activityID, String deviceID, String what, String description, String when, double latitude, double longitude, ActivitySettingsModel settings)
    {
        this.DeviceID = deviceID;
        this.What = what;
        this.When = when;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.description = description;
        this.activitySetting = settings;
    }

    @SerializedName("ActivityID")
    public String ActivityID;
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
    @SerializedName("ImagePath")
    public String ImagePath;
    @SerializedName("ActivitySetting")
    public ActivitySettingsModel activitySetting;

    protected ActivityModel(Parcel in) {
        ActivityID = in.readString();
        DeviceID = in.readString();
        What = in.readString();
        When = in.readString();
        Latitude = in.readDouble();
        Longitude = in.readDouble();
        description = in.readString();
        ImagePath = in.readString();
        activitySetting = in.readParcelable(ActivitySettingsModel.class.getClassLoader());
    }

    public static final Creator<ActivityModel> CREATOR = new Creator<ActivityModel>() {
        @Override
        public ActivityModel createFromParcel(Parcel in) {
            return new ActivityModel(in);
        }

        @Override
        public ActivityModel[] newArray(int size) {
            return new ActivityModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ActivityID);
        parcel.writeString(DeviceID);
        parcel.writeString(What);
        parcel.writeString(When);
        parcel.writeDouble(Latitude);
        parcel.writeDouble(Longitude);
        parcel.writeString(description);
        parcel.writeString(ImagePath);
        parcel.writeParcelable(activitySetting, i);
    }
}
