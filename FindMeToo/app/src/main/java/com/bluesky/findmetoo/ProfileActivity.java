package com.bluesky.findmetoo;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.bluesky.findmetoo.ServiceInterfaces.ApiInterface;
import com.bluesky.findmetoo.ServiceInterfaces.RegisterBindingModel;
import com.bluesky.findmetoo.ServiceInterfaces.TokenBindingModel;
import com.bluesky.findmetoo.model.CurrentActivity;
import com.bluesky.findmetoo.model.ProfileModel;
import com.bluesky.findmetoo.model.Token;
import com.bluesky.findmetoo.preference.PrefConst;
import com.bluesky.findmetoo.uitls.GPSTracker;
import com.bluesky.findmetoo.uitls.Global;
import com.bluesky.findmetoo.uitls.HttpClient;
import com.bluesky.findmetoo.uitls.SQLHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);

        fillProfile();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_save:
                addProfile();
                break;
        }

    }

    private void fillProfile()
    {
        String deviceId = String.valueOf(Global.current_user.getId());
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        final EditText txtProfileName = ((EditText) findViewById(R.id.txt_name));
        final EditText txtHobies = ((EditText) findViewById(R.id.txt_hobies));
        final EditText txtAbout = ((EditText) findViewById(R.id.txt_about));
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ProfileModel> call = apiService.getProfile("Bearer " + token, deviceId);
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if(response.isSuccessful()) {
                    ProfileModel profileModel = response.body();
                    if(profileModel!=null) {
                        txtProfileName.setText(profileModel.getProfileName());
                        txtHobies.setText(profileModel.getHobies());
                        txtAbout.setText(profileModel.getAbout());
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
            }
        });

    }

    private void addProfile() {
        String deviceId = String.valueOf(Global.current_user.getId());
        String profilName = ((EditText) findViewById(R.id.txt_name)).getText().toString();
        String hobies = ((EditText) findViewById(R.id.txt_hobies)).getText().toString();
        String about = ((EditText) findViewById(R.id.txt_about)).getText().toString();
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");

        ProfileModel profileModel = new ProfileModel(String.valueOf(Global.current_user.getId()), Global.preference.getValue(this,
                PrefConst.USERNAME, ""),
                null, profilName, hobies, about);

        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.putProfile("Bearer " + token, deviceId, profileModel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }
}
