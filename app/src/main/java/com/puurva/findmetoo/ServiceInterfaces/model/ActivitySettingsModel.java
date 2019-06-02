package com.puurva.findmetoo.ServiceInterfaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.puurva.findmetoo.Enums.ActivityStatuses;
import com.puurva.findmetoo.Enums.ActivityTypes;

import java.util.Date;

public class ActivitySettingsModel implements Parcelable {

    @SerializedName("ActivityId")
    public String ActivityId;

    @SerializedName("StartTime")
    public String StartTime;

    @SerializedName("EndTime")
    public String EndTime;

    @SerializedName("ActivityType")
    public ActivityTypes ActivityType;

    @SerializedName("ActivityStatus")
    public ActivityStatuses ActivityStatus;

    @SerializedName("ActivityReviews")
    public long ActivityReviews;

    @SerializedName("ActivityViews")
    public long ActivityViews;

    @SerializedName("Comments")
    public String Comments;


    public ActivitySettingsModel(String activityId, String startTime, String endTime, ActivityTypes activityType,
                                 ActivityStatuses activityStatus, long activityReviews, long activityViews, String comments) {
        this.ActivityId = activityId;
        this.StartTime = startTime;
        this.EndTime = endTime;
        this.ActivityType = activityType;
        this.ActivityStatus = activityStatus;
        this.ActivityReviews = activityReviews;
        this.ActivityViews = activityViews;
        this.Comments = comments;
    }

    protected ActivitySettingsModel(Parcel in) {
        ActivityId = in.readString();
        StartTime = in.readString();
        EndTime = in.readString();
        ActivityReviews = in.readLong();
        ActivityViews = in.readLong();
        Comments = in.readString();
        ActivityType = ActivityTypes.valueOf(in.readString());
        ActivityStatus = ActivityStatuses.valueOf(in.readString());
    }

    public static final Creator<ActivitySettingsModel> CREATOR = new Creator<ActivitySettingsModel>() {
        @Override
        public ActivitySettingsModel createFromParcel(Parcel in) {
            return new ActivitySettingsModel(in);
        }

        @Override
        public ActivitySettingsModel[] newArray(int size) {
            return new ActivitySettingsModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ActivityId);
        parcel.writeString(StartTime);
        parcel.writeString(EndTime);
        parcel.writeLong(ActivityReviews);
        parcel.writeLong(ActivityViews);
        parcel.writeString(Comments);
        parcel.writeString(ActivityType.name());
        parcel.writeString(ActivityStatus != null ? ActivityStatus.name() : ActivityStatuses.OPEN.name());
    }
}

