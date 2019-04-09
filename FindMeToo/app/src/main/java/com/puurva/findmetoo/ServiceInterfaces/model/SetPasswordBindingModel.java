package com.puurva.findmetoo.ServiceInterfaces.model;

import com.google.gson.annotations.SerializedName;

public class SetPasswordBindingModel {

    public SetPasswordBindingModel(String password, String confirmPassword)
    {
        this.Password = password;
        this.ConfirmPassword = confirmPassword;
    }

    @SerializedName("Password")
    private String Password;
    @SerializedName("ConfirmPassword")
    private String ConfirmPassword;
}

