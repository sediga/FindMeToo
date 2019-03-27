package com.puurva.findmetoo.model;

import com.google.gson.annotations.SerializedName;
import com.puurva.findmetoo.Enums.ActivityStatuses;
import com.puurva.findmetoo.Enums.ActivityTypes;

import java.util.Date;

public class ActivitySettingsModel {

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
}

