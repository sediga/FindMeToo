package com.puurva.findmetoo.Activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.RegisterBindingModel;
import com.puurva.findmetoo.ServiceInterfaces.model.SetPasswordBindingModel;
import com.puurva.findmetoo.ServiceInterfaces.model.Token;
import com.puurva.findmetoo.ServiceInterfaces.model.TokenBindingModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.SQLHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPassword extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        findViewById(R.id.button_reset).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.button_reset:
                final String password = ((EditText) findViewById(R.id.edit_password)).getText().toString();
                String confirm = ((EditText) findViewById(R.id.edit_confirm)).getText().toString();
                resetPassword(password, confirm);
                break;
            case R.id.button_cancel:
                finish();
                break;
        }

    }

    private void resetPassword(final String password, final String confirmPassword) {
        validate(password, confirmPassword);
        SetPasswordBindingModel setPasswordBindingModel = new SetPasswordBindingModel(password, confirmPassword);
        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        final ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.setPassword(token, setPasswordBindingModel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
//                System.out.println(response.toString());
                if (response.isSuccessful()) {
//                    TokenBindingModel tokenBindingModel = new TokenBindingModel(email, "password", password);
//                    Call<Token> tokenCall = apiService.getToken(email, password, "password");
//                    tokenCall.enqueue((new Callback<Token>() {
//                        @Override
//                        public void onResponse(Call<Token> call, Response<Token> response) {
//                            Token token = response.body();
//                            String softwareVersion = Build.VERSION.RELEASE;
//                            DeviceModel deviceModel = new DeviceModel(Global.AndroidID, email, softwareVersion, null);
//
//                            final ApiInterface apiService =
//                                    HttpClient.getClient().create(ApiInterface.class);
////        TokenBindingModel tokenBindingModel = new TokenBindingModel(username, "password", password);
//                            Call<Void> tokenCall = apiService.postDevice(deviceModel);
//                            tokenCall.enqueue((new Callback<Void>() {
//                                @Override
//                                public void onResponse(Call<Void> call, Response<Void> response) {
//                                    if (response.isSuccessful()) {
//                                        Global.has_device_registered = true;
//                                        finish();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<Void> call, Throwable t) {
//                                    System.out.println(t.getMessage());
//                                    Log.e("login", "Login Failed : " + t.getMessage());
//                                }
//                            }));
//                        }
//
//                        @Override
//                        public void onFailure(Call<Token> call, Throwable t) {
//                            System.out.println(t.getMessage());
//                        }
//                    }));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }

    private void validate(String password, String confirm ) {


        if (password.isEmpty() || confirm.isEmpty()) {
            Global.showShortToast(this, "User info is required.");
            return;
        }

        if (!password.equals(confirm)) {
            Global.showShortToast(this, "Password not match.");
            return;
        }

//        finish();
    }

    private void InsertUser(Location location, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        values.put("Lat", location.getLatitude());
        values.put("Long", location.getLongitude());

        SQLHelper.Insert("t_user", values);
    }

}
