package com.puurva.findmetoo.ServiceInterfaces.model;

import com.google.gson.annotations.SerializedName;

public class Token {
    public Token() {
    }

    @SerializedName("access_token")
    public String access_token;
    @SerializedName("token_type")
    public String token_type;
    @SerializedName("expires_in")
    public int expires_in;
    @SerializedName("userName")
    public String userName;
    //public string __invalid_name__.issued { get; set; }
    //public string __invalid_name__.expires { get; set; }
}