package com.puurva.findmetoo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.DeviceModel;
import com.puurva.findmetoo.model.Token;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.preference.Preference;
import com.puurva.findmetoo.uitls.CallBackHelper;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.SQLHelper;
import com.puurva.findmetoo.uitls.SQLiteManager;
import com.puurva.findmetoo.model.UserModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private SQLiteManager dbHelper;
    private String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);

        if(!Global.preference.getValue(this, PrefConst.HASREQUIREDPERMISSIONS, false)) {
            if (confirmationPermission()) {
                // sqlite db_user setting
                dbHelper = new SQLiteManager(this);
                Global.mdb = dbHelper.openDataBase();
            }
        }

        Global.preference = Preference.getInstance();
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String password = Global.preference.getValue(this, PrefConst.PASSWORD, "");

        androidId = Global.preference.getValue(this, PrefConst.ANDROIDID, "");
        if (androidId == "") {
            androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Global.preference.put(this, PrefConst.ANDROIDID, androidId);
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                if(!Global.has_device_registered) {
                    while(Global.mdb == null);
                    String newToken = instanceIdResult.getToken();
                    String softwareVersion = Build.VERSION.RELEASE;
                    DeviceModel latestStoredDevice = SQLHelper.GetLatestDevice();
                    if (latestStoredDevice == null || latestStoredDevice.NotificationToken.compareTo(newToken) != 0) {
                        DeviceModel deviceModel = new DeviceModel(androidId, "", softwareVersion, newToken);
                        CommonUtility.RegisterDevice(deviceModel);
                        SQLHelper.AddDevice(deviceModel);
                    }
                }
            }
        });

        if (!username.isEmpty() && !password.isEmpty()) {
//            EditText edit_username = findViewById(R.id.edit_username);
//            EditText edit_password = findViewById(R.id.edit_password);
//            edit_username.setText(username);
//            edit_password.setText(password);
            doLogin(username, password);
        }
    }

    /**
     *  confirm permission
     */
    private boolean confirmationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    Global.PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == Global.PERMISSION_REQUEST_CODE) {
            for (int grantResult : grantResults) {
                // check permission result
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission error", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            Global.preference.put(this, PrefConst.HASREQUIREDPERMISSIONS, true);
            // sqlite db_user setting
            dbHelper = new SQLiteManager(this);
            Global.mdb = dbHelper.openDataBase();
        }
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.button_login:

                EditText edit_username = findViewById(R.id.edit_username);
                EditText edit_password = findViewById(R.id.edit_password);

                if (edit_username.getText().toString().isEmpty()) {
                    Global.showShortToast(this, "First name is required.");
                    edit_username.requestFocus();
                    return;
                }

                if (edit_password.getText().toString().isEmpty()) {
                    Global.showShortToast(this, "Password is empty");
                    edit_password.requestFocus();
                    return;
                }

                doLogin(edit_username.getText().toString(), edit_password.getText().toString());

                break;
            case R.id.button_register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }

    /**
     *  confirm permission
     */
    private void loginToAPI(final String username, final String password, final CallBackHelper helper)
    {
       final ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
//        TokenBindingModel tokenBindingModel = new TokenBindingModel(username, "password", password);
        Call<Token> tokenCall = apiService.getToken(username, password, "password");
        tokenCall.enqueue((new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if(response.isSuccessful()) {
                    helper.registerToken(response.body());
                    if (!getUser(username, password)) {
                        return;
                    }
                    startActivity(new Intent(LoginActivity.this, MapsActivity.class));
//                    finish();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                System.out.println(t.getMessage());
                Toast.makeText(LoginActivity.this   ,"Login Failed" + t.getMessage(), Toast.LENGTH_SHORT );
                Log.e("login", "Login Failed : " + t.getMessage());
            }
        }));
    }

    private void doLogin(final String username, String password) {

        CallBackHelper callBackHelper = new CallBackHelper() {
            @Override
            public void registerToken(Token token) {
                ContentValues values = new ContentValues();
                values.put("deviceid", token.userName);
                values.put("token", token.access_token);
                SQLHelper.Insert("apiuser", values);
                saveToken(token.access_token);
            }
        };
        this.loginToAPI(username, password, callBackHelper);
//        if (!getUser(username, password)) return;
    }

    private boolean getUser(String username, String password) {
        Cursor c = Global.mdb.rawQuery(
                "SELECT *    " +
                        "FROM t_user " +
                        "WHERE email = '" + username + "' AND password = '" + password + "' " +
                        "LIMIT 1",
                     null);

        if (c == null || c.getCount() == 0) {
            Global.showShortToast(this, "user name or password is invalid.");
            return false;
        }

        Global.preference.put(this, PrefConst.USERNAME, username);
        Global.preference.put(this, PrefConst.PASSWORD, password);

        c.moveToFirst();
        Global.current_user = new UserModel(
                c.getInt(0),
                c.getString(1),
                c.getString(2),
                c.getDouble(3),
                c.getDouble(4)
        );
        return true;
    }

    private void saveToken(String token) {

        String savedToken = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (savedToken == null || savedToken == "") {
                Global.preference.put(this, PrefConst.TOKEN, token);
        }
    }
}
