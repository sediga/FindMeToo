package com.puurva.findmetoo;

import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.model.Token;
import com.puurva.findmetoo.uitls.GPSTracker;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.puurva.findmetoo.ServiceInterfaces.*;
import com.puurva.findmetoo.uitls.SQLHelper;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_profile).setOnClickListener(this);
        findViewById(R.id.button_login).setOnClickListener(this);

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

    private void registerApiUser(final String email, final String password)
    {
        Token token = null;
        RegisterBindingModel registerBindingModel = new RegisterBindingModel(email, password, password);
        final ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.registerExternal(registerBindingModel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
//                System.out.println(response.toString());
                if(response.isSuccessful())
                {
                    TokenBindingModel tokenBindingModel = new TokenBindingModel(email, "password", password);
                    Call<Token> tokenCall = apiService.getToken(email, password, "password");
                    tokenCall.enqueue((new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            Token token = response.body();
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

        SQLHelper.Insert("t_user", values);

        Global.showShortToast(this, "User registered successfully.");
        this.registerApiUser(email, password);
        finish();
    }

}
