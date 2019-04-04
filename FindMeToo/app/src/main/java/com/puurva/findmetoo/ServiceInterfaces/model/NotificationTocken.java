package com.puurva.findmetoo.ServiceInterfaces.model;

import com.google.gson.annotations.SerializedName;

public class NotificationTocken {
    public NotificationTocken() {
    }

    @SerializedName("device_id")
    public String device_id;
    @SerializedName("token")
    public String token;
}