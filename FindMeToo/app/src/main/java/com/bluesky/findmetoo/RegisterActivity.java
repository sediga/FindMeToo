package com.bluesky.findmetoo;

import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.bluesky.findmetoo.ServiceInterfaces.ApiInterface;
import com.bluesky.findmetoo.model.CurrentActivity;
import com.bluesky.findmetoo.model.Token;
import com.bluesky.findmetoo.uitls.GPSTracker;
import com.bluesky.findmetoo.uitls.Global;
import com.bluesky.findmetoo.uitls.HttpClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.bluesky.findmetoo.ServiceInterfaces.*;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_login).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.button_register:
                doRegister();
                break;
            case R.id.button_login:
                finish();
                break;
        }

    }

    private void registerApiUser(String email, String password)
    {
        RegisterBindingModel registerBindingModel = new RegisterBindingModel(email, password, password);
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Token> call = apiService.registerExternal(registerBindingModel);
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                Token token = response.body();
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
            }
        });
    }

    private void doRegister() {

        String email = ((EditText) findViewById(R.id.edit_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.edit_password)).getText().toString();
        String confirm = ((EditText) findViewById(R.id.edit_confirm)).getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Global.showShortToast(this, "User info is required.");
            return;
        }

        if (!password.equals(confirm)) {
            Global.showShortToast(this, "Password not match.");
            return;
        }

        GPSTracker gpsTracker = new GPSTracker(this);
        gpsTracker.getLocation();

        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        values.put("latitude", gpsTracker.latitude);
        values.put("longitude", gpsTracker.longitude);
        Global.mdb.insert("t_user", null, values);

        Global.showShortToast(this, "User registered successfully.");
        this.registerApiUser(email, password);
        finish();
    }

}
