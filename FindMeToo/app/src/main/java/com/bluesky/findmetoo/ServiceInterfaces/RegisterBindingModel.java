package com.bluesky.findmetoo.ServiceInterfaces;

import com.google.gson.annotations.SerializedName;

public class RegisterBindingModel {

    public RegisterBindingModel(String email, String password, String confirmPassword)
    {
        this.Email = email;
        this.Password = password;
        this.ConfirmPassword = confirmPassword;
    }

    @SerializedName("Email")
    private String Email;
    @SerializedName("Password")
    private String Password;
    @SerializedName("ConfirmPassword")
    private String ConfirmPassword;
}

