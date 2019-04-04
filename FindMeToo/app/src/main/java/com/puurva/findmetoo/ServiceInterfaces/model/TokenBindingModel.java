package com.puurva.findmetoo.ServiceInterfaces.model;

import com.google.gson.annotations.SerializedName;

public class TokenBindingModel {

    public TokenBindingModel(String userName, String grantType, String password)
    {
        this.UserName = userName;
        this.Password = password;
        this.GrantType = grantType;
    }

    @SerializedName("grant_type")
    private String GrantType;
    @SerializedName("password")
    private String Password;
    @SerializedName("username")
    private String UserName;
}

