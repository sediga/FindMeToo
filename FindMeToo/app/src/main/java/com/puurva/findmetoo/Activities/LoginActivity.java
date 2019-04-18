package com.puurva.findmetoo.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityNotification;
import com.puurva.findmetoo.ServiceInterfaces.model.Token;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.preference.Preference;
import com.puurva.findmetoo.uitls.CallBackHelper;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.SQLHelper;
import com.puurva.findmetoo.uitls.SQLiteManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private SQLiteManager dbHelper;
    private String androidId;
    private ActivityNotification activityNotification = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString("FromDeviceId") != null &&
                bundle.getString("ActivityId") != null &&
                bundle.getString("NotificationRequestStatus") != null &&
                bundle.getString("RequestNotificationType") != null) {
            activityNotification = new ActivityNotification(bundle.getString("FromDeviceId"),
                    bundle.getString("ActivityId"),
                    RequestStatus.valueOf(bundle.getString("NotificationRequestStatus")),
                    NotificationType.valueOf(bundle.getString("RequestNotificationType")));
            getIntent().putExtra("ActivityNotification", activityNotification);
        }
        activityNotification = getIntent().getParcelableExtra("ActivityNotification");
        if(!Global.is_loggedin) {
            findViewById(R.id.button_login).setOnClickListener(this);
            findViewById(R.id.button_register).setOnClickListener(this);
            findViewById(R.id.button_change_password).setOnClickListener(this);

            while (!confirmationPermission()) ;
        }else{
            LoadActivity();
        }
        // sqlite db_user setting

    }

    @Override
    public void onResume(){
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_change_password).setOnClickListener(this);
        super.onResume();
        // put your code here...

    }
    private void DoPostPermissionOperations() {
        Global.preference = Preference.getInstance();
        if(!Global.has_device_registered){
            Global.preference.remove(this, PrefConst.USERNAME);
            Global.preference.remove(this, PrefConst.PASSWORD);
        }
        final String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String password = Global.preference.getValue(this, PrefConst.PASSWORD, "");

        androidId = Global.AndroidID;
        if (androidId == null) {
            androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            Global.AndroidID = androidId;
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                if (!Global.has_device_registered) {
                    while (Global.mdb == null) ;
                    String newToken = instanceIdResult.getToken();
                    String softwareVersion = Build.VERSION.RELEASE;
                    DeviceModel latestStoredDevice = SQLHelper.GetLatestDevice();
                    if (latestStoredDevice == null || latestStoredDevice.NotificationToken.compareTo(newToken) != 0) {
                        DeviceModel deviceModel = new DeviceModel(androidId, username, softwareVersion, newToken);
                        CommonUtility.RegisterDevice(deviceModel);
                    }
                }
            }
        });

        if (!username.isEmpty() && !password.isEmpty()) {
            EditText edit_username = findViewById(R.id.edit_username);
            EditText edit_password = findViewById(R.id.edit_password);
            edit_username.setText(username);
            edit_password.setText(password);
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
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    Global.PERMISSION_REQUEST_CODE);
            return false;
        } else {
            dbHelper = new SQLiteManager(this);
            Global.mdb = dbHelper.openDataBase();

            DoPostPermissionOperations();
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
            // sqlite db_user setting
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
            case  R.id.button_change_password:
                startActivity(new Intent(this, ResetPassword.class));
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
                    helper.onCallBack(new Object[] { response.body() });
                    if (!SQLHelper.getUser(username, password)) {
                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_keyword, null);
                        builder.setTitle("Login failed")
                                .setMessage("Sorry, Email not found, try registering.")
                                .setCancelable(false)
                                .setPositiveButton("OK", null);
                        builder.show();
                        return;
                    }
                    Global.is_loggedin = true;
                    LoadActivity();
//                    finish();
                } else if(response.raw().code() == 400) {
                    Global.showAlert(LoginActivity.this, "Login failed",
                            "Email or Password is wrong, please try again. If you do not have account, you need to creat one.");
//                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                    View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_keyword, null);
//                    builder.setTitle("Login failed")
//                            .setMessage("Email or Password is wrong, please try again. If you do not have account, you need to creat one.")
//                            .setCancelable(false)
//                            .setPositiveButton("OK", null);
//                    builder.show();
                } else {
                    Global.showAlert(LoginActivity.this, "Login failed",
                            "Oops! Something went wrong, please try again");

//                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                    View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_keyword, null);
//                    builder.setTitle("Login failed")
//                            .setMessage("Oops! Something went wrong, please try again")
//                            .setCancelable(false)
//                            .setPositiveButton("OK", null);
//                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                System.out.println(t.getMessage());
                Global.showAlert(LoginActivity.this, "Login failed", "Oops! Something went wrong, please try again");
//                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
////                View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_keyword, null);
//                builder.setTitle("Login failed")
//                        .setMessage("Oops! Something went wrong, please try again")
//                        .setCancelable(false)
//                        .setPositiveButton("OK", null);
//                builder.show();
//                Toast.makeText(LoginActivity.this   ,"Login Failed" + t.getMessage(), Toast.LENGTH_SHORT );
                Log.e("login", "Login Failed : " + t.getMessage());
            }
        }));
    }

    private void LoadActivity() {
        Intent intentClass = new Intent(LoginActivity.this, MapsActivity.class);
//        Log.e("LoginActivity", "checking activityNotificaiton");
        if(activityNotification != null) {
//            Log.e("LoginActivity", "activityNotificaiton not null");
//            switch (activityNotification.ActivityRequestStatus) {
//                case NEW:
////                    intentClass = new Intent(LoginActivity.this, ProfileViewActivity.class);
////                    break;
//                case REJECTED:
//                case ACCEPTED:
//                    intentClass = new Intent(LoginActivity.this, MapsActivity.class);
//                    break;
//            }
            intentClass.putExtra("ActivityNotification", activityNotification);
        }else{
            Log.e("LoginActivity", "activityNotificaiton is null");
        }
        startActivity(intentClass);
    }

    private void doLogin(final String username, String password) {

        CallBackHelper callBackHelper = new CallBackHelper() {
            @Override
            public void onCallBack(Object[] returnObjects) {
                if(returnObjects !=null && returnObjects.length > 0) {
                    Token token = (Token)returnObjects[0];
                    SQLHelper.RegisterToken(token);
                    saveToken(token.access_token);
                }
            }
        };
        this.loginToAPI(username, password, callBackHelper);
//        if (!getUser(username, password)) return;
    }

    private void saveToken(String token) {

        if (Global.TOKEN == null || Global.TOKEN.compareTo(token) != 0) {
            Global.preference.put(this, PrefConst.TOKEN, token);
            Global.TOKEN = token;
        }
    }
}
  