package com.bluesky.findmetoo.model;

import com.google.gson.annotations.SerializedName;

public class ProfileModel {
    public ProfileModel(){

    }

    public ProfileModel(String deviceId, String userName, byte[] profilePhoto, String profileName, String hobies, String about, byte rating, long reviews, long views){
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

    public ProfileModel(String deviceId, String userName, byte[] profilePhoto, String profileName, String hobies, String about){
        this.DeviceId = deviceId;
        this.UserName = userName;
        this.ProfilePhoto = profilePhoto;
        this.ProfileName = profileName;
        this.Hobies = hobies;
        this.About = about;
    }

    @SerializedName("DeviceId")
    private String DeviceId;

    @SerializedName("UserName")
    private String UserName;

    @SerializedName("ProfilePhoto")
    private byte[] ProfilePhoto;

    @SerializedName("ProfileName")
    private String ProfileName;

    @SerializedName("Hobies")
    private String Hobies;

    @SerializedName("About")
    private String About;

    @SerializedName("Rating")
    private byte Rating;

    @SerializedName("Reviews")
    private long Reviews;

    @SerializedName("views")
    private long Views;

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

    public byte[] getProfilePhoto() {
        return ProfilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
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

    public byte getRating() {
        return Rating;
    }

    public void setRating(byte rating) {
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
}
