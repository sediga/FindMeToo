package com.puurva.findmetoo.ServiceInterfaces.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ProfileModel implements Parcelable {
    public ProfileModel(){

    }

    public ProfileModel(String deviceId, String userName, Bitmap profilePhoto, String profileName, String hobies, String about, float rating, long reviews, long views){
        this.DeviceId = deviceId;
        this.UserName = userName;
        this.ProfilePhoto = profilePhoto;
        this.ProfileName = profileName;
        this.Hobies = hobies;
        this.About = about;
        this.Rating = rating;
        this.Reviews = reviews;
        this.Views = views;
    }

    public ProfileModel(String deviceId, String userName, Bitmap profilePhoto, String profileName, String hobies, String about){
        this.DeviceId = deviceId;
        this.UserName = userName;
        this.ProfilePhoto = profilePhoto;
        this.ProfileName = profileName;
        this.Hobies = hobies;
        this.About = about;
    }

    @SerializedName("DeviceID")
    private String DeviceId;

    @SerializedName("UserName")
    private String UserName;

    @SerializedName("ProfilePhoto")
    private Bitmap ProfilePhoto;

    @SerializedName("ProfileName")
    private String ProfileName;

    @SerializedName("Hobies")
    private String Hobies;

    @SerializedName("About")
    private String About;

    @SerializedName("Rating")
    private float Rating;

    @SerializedName("Reviews")
    private long Reviews;

    @SerializedName("views")
    private long Views;

    protected ProfileModel(Parcel in) {
        DeviceId = in.readString();
        UserName = in.readString();
        if (in.readByte() == 1) {
            ProfilePhoto = (Bitmap) in.readParcelable(Bitmap.class.getClassLoader());
        }
//        ProfilePhoto = in.readParcelable(Bitmap.class.getClassLoader());
        ProfileName = in.readString();
        Hobies = in.readString();
        About = in.readString();
        Rating = in.readByte();
        Reviews = in.readLong();
        Views = in.readLong();
    }

    public static final Creator<ProfileModel> CREATOR = new Creator<ProfileModel>() {
        @Override
        public ProfileModel createFromParcel(Parcel in) {
            return new ProfileModel(in);
    }

        @Override
        public ProfileModel[] newArray(int size) {
            return new ProfileModel[size];
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

    public Bitmap getProfilePhoto() {
        return ProfilePhoto;
    }

    public void setProfilePhoto(Bitmap profilePhoto) {
        ProfilePhoto = profilePhoto;
    }

    public String getProfileName() {
        return ProfileName;
    }

    public void setProfileName(String profileName) {
        ProfileName = profileName;
    }

    public String getHobies() {
        return Hobies;
    }

    public void setHobies(String hobies) {
        Hobies = hobies;
    }

    public String getAbout() {
        return About;
    }

    public void setAbout(String about) {
        About = about;
    }

    public float getRating() {
        return Rating;
    }

    public void setRating(float rating) {
        Rating = rating;
    }

    public long getReviews() {
        return Reviews;
    }

    public void setReviews(long reviews) {
        Reviews = reviews;
    }

    public long getViews() {
        return Views;
    }

    public void setViews(long views) {
        this.Views = views;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(DeviceId);
        parcel.writeString(UserName);
        if (ProfilePhoto != null){
            parcel.writeByte((byte) 1);
            parcel.writeParcelable(ProfilePhoto,i);
        } else {
            parcel.writeByte((byte) 0);
        }
//        parcel.writeValue(ProfilePhoto);
        parcel.writeString(ProfileName);
        parcel.writeString(Hobies);
        parcel.writeString(About);
        parcel.writeFloat(Rating);
        parcel.writeLong(Reviews);
        parcel.writeLong(Views);
    }
}
