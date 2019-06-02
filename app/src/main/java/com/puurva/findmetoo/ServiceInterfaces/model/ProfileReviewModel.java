package com.puurva.findmetoo.ServiceInterfaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ProfileReviewModel implements Parcelable {
    public ProfileReviewModel(){

    }

    public ProfileReviewModel(String fromDeviceId, String deviceId, String userName, String  review, float rating){
        this.FromDeviceId = fromDeviceId;
        this.DeviceId = deviceId;
        this.UserName = userName;
        this.Review = review;
        this.Rating = rating;
    }

    @SerializedName("FromDeviceId")
    private String FromDeviceId;

    @SerializedName("DeviceId")
    private String DeviceId;

    @SerializedName("UserName")
    private String UserName;

    @SerializedName("Review")
    private String Review;

    @SerializedName("Rating")
    private float Rating;

    protected ProfileReviewModel(Parcel in) {
        FromDeviceId = in.readString();
        DeviceId = in.readString();
        UserName = in.readString();
        Review = in.readString();
        Rating = in.readInt();
    }

    public static final Creator<ProfileReviewModel> CREATOR = new Creator<ProfileReviewModel>() {
        @Override
        public ProfileReviewModel createFromParcel(Parcel in) {
            return new ProfileReviewModel(in);
    }

        @Override
        public ProfileReviewModel[] newArray(int size) {
            return new ProfileReviewModel[size];
        }
    };

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getReview() {
        return Review;
    }

    public void setReview(String review) {
        Review = review;
    }

    public float getRating() {
        return Rating;
    }

    public void setRating(int rating) {
        Rating = rating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(FromDeviceId);
        parcel.writeString(DeviceId);
        parcel.writeString(UserName);
        parcel.writeString(Review);
        parcel.writeFloat(Rating);
    }
}
