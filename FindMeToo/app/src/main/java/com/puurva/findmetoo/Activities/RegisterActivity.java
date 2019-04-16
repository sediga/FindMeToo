package com.puurva.findmetoo.Activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.RegisterBindingModel;
import com.puurva.findmetoo.ServiceInterfaces.model.Token;
import com.puurva.findmetoo.ServiceInterfaces.model.TokenBindingModel;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.puurva.findmetoo.uitls.SQLHelper;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_profile).setOnClickListener(this);
        findViewById(R.id.button_login).setOnClickListener(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.button_register:
                doRegister();
                break;
            case R.id.button_profile:
                addProfile();
                break;
            case R.id.button_login:
                finish();
                break;
        }

    }

    private void addProfile() {
        setContentView(R.layout.activity_profile);
    }

    private void registerApiUser(final String email, final String password) {
        Token token = null;
        RegisterBindingModel registerBindingModel = new RegisterBindingModel(email, password, password);
        final ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.registerExternal(registerBindingModel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
//                System.out.println(response.toString());
                if (response.isSuccessful()) {
                    TokenBindingModel tokenBindingModel = new TokenBindingModel(email, "password", password);
                    Call<Token> tokenCall = apiService.getToken(email, password, "password");
                    tokenCall.enqueue((new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            Token token = response.body();
                            String softwareVersion = Build.VERSION.RELEASE;
                            DeviceModel deviceModel = new DeviceModel(Global.AndroidID, email, softwareVersion, null);

                            final ApiInterface apiService =
                                    HttpClient.getClient().create(ApiInterface.class);
//        TokenBindingModel tokenBindingModel = new TokenBindingModel(username, "password", password);
                            Call<Void> tokenCall = apiService.postDevice(deviceModel);
                            tokenCall.enqueue((new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Global.has_device_registered = true;
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    System.out.println(t.getMessage());
                                    Log.e("login", "Login Failed : " + t.getMessage());
                                }
                            }));
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            System.out.println(t.getMessage());
                        }
                    }));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }

    private void doRegister() {

        final String email = ((EditText) findViewById(R.id.edit_email)).getText().toString();
        final String password = ((EditText) findViewById(R.id.edit_password)).getText().toString();
        String confirm = ((EditText) findViewById(R.id.edit_confirm)).getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Global.showShortToast(this, "User info is required.");
            return;
        }

        if (!password.equals(confirm)) {
            Global.showShortToast(this, "Password not match.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            UpsertUser(location, email, password);

                            RegisterActivity.this.registerApiUser(email, password);

                            Global.showShortToast(RegisterActivity.this, "User registered successfully.");                        }
                    }

                });


//        finish();
    }

    private void UpsertUser(Location location, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        values.put("Lat", location.getLatitude());
        values.put("Long", location.getLongitude());

        SQLHelper.Insert("t_user", values);
    }

}
