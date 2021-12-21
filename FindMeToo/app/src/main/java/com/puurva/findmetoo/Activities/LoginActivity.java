package com.puurva.findmetoo.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityNotification;
import com.puurva.findmetoo.ServiceInterfaces.model.Token;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.preference.Preference;
import com.puurva.findmetoo.uitls.CallBackHelper;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.DBUtils;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.SQLHelper;
import com.puurva.findmetoo.uitls.SQLiteManager;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;
    private SQLiteManager dbHelper;
    private String androidId;
    private ActivityNotification activityNotification = null;
    private String activityOfNotification = null;
    private String notificationId = null;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null && bundle.getString("FromDeviceId") != null &&
//                bundle.getString("ActivityId") != null &&
//                bundle.getString("NotificationRequestStatus") != null &&
//                bundle.getString("RequestNotificationType") != null) {
//            activityNotification = new ActivityNotification(bundle.getString("FromDeviceId"),
//                    bundle.getString("ActivityId"),
//                    RequestStatus.valueOf(bundle.getString("NotificationRequestStatus")),
//                    NotificationType.valueOf(bundle.getString("RequestNotificationType")));
//            getIntent().putExtra("ActivityNotification", activityNotification);
//        }
        activityNotification = getIntent().getParcelableExtra("ActivityNotification");
        activityOfNotification = getIntent().getStringExtra("ActivityOfNotification");
        notificationId = getIntent().getStringExtra("NotificationId");
        if(!Global.is_loggedin) {
            findViewById(R.id.button_login).setOnClickListener(this);
            findViewById(R.id.button_register).setOnClickListener(this);
            findViewById(R.id.button_change_password).setOnClickListener(this);

            while (!confirmationPermission()) ;
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                InitializeAuthentication();
//                userId = auth.getCurrentUser().getUid();
//                DBUtils.AddNewUser(userId, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getEmail(), 1, notificationId);
//                currentUser = new User(userId, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getEmail(), notificationId);
//                DBUtils.AddOnlineUser(currentUser);
//                Global.preference.put(this, "userid", userId);
//            StartNextActivity();
//            DBUtils.DeleteUserGames(userId);
            } else {
                InitializeAuthentication();
            }
        }else{

            //            LoadActivity();
        }
        // sqlite db_user setting

    }

    @Override
    public void onResume(){
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_change_password).setOnClickListener(this);
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String password = Global.preference.getValue(this, PrefConst.PASSWORD, "");
        if (!username.isEmpty() && !password.isEmpty()) {
            EditText edit_username = findViewById(R.id.edit_username);
            EditText edit_password = findViewById(R.id.edit_password);
            edit_username.setText(username);
            edit_password.setText(password);
//            doLogin(username, password);
        }
        super.onResume();
        // put your code here...

    }
    private void DoPostPermissionOperations() {
        Global.preference = Preference.getInstance();
//        if(!Global.has_device_registered){
//            Global.preference.remove(this, PrefConst.USERNAME);
//            Global.preference.remove(this, PrefConst.PASSWORD);
//        }
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
                String newToken = instanceIdResult.getToken();
                String existingToken = Global.preference.getValue(LoginActivity.this, "firebase_token", null);
                if (existingToken == null || newToken.compareTo(existingToken) != 0) {
                    while (Global.mdb == null) ;
                    String softwareVersion = Build.VERSION.RELEASE;
                    DeviceModel latestStoredDevice = SQLHelper.GetLatestDevice();
                    if (latestStoredDevice == null || latestStoredDevice.NotificationToken.compareTo(newToken) != 0) {
                        DeviceModel deviceModel = new DeviceModel(androidId, username, softwareVersion, newToken);
                        CommonUtility.RegisterDevice(deviceModel);
                        Global.preference.put(LoginActivity.this, "firebase_token", newToken);
                    }
                }
            }
        });

        if (!username.isEmpty() && !password.isEmpty()) {
            EditText edit_username = findViewById(R.id.edit_username);
            EditText edit_password = findViewById(R.id.edit_password);
            edit_username.setText(username);
            edit_password.setText(password);
//            doLogin(username, password);
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
            DoPostPermissionOperations();
            String isFirstTime = Global.preference.getValue(LoginActivity.this, "is_firsttime", null);
            if(isFirstTime == null){
                dbHelper.deleteDataBase();
                Global.preference.put(LoginActivity.this, "is_firsttime", "false");
            }
            Global.mdb = dbHelper.openDataBase();

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            setIntent(null);
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void LoadActivity() {
        Intent intentClass = new Intent(LoginActivity.this, MapsActivity.class);
//        Log.e("LoginActivity", "checking activityNotificaiton");
        if(activityNotification != null) {
            intentClass.putExtra("ActivityNotification", activityNotification);
        }
        if(activityOfNotification != null) {
            intentClass.putExtra("ActivityOfNotification", activityOfNotification);
        }
        if(notificationId != null) {
            intentClass.putExtra("NotificationId", notificationId);
        }

        startActivity(intentClass);
    }

    private void doLogin(final String email, final String password) {

        CallBackHelper callBackHelper = new CallBackHelper() {
            @Override
            public void onCallBack(Object[] returnObjects) {
                if(returnObjects !=null && returnObjects.length > 0) {
                    Token token = (Token)returnObjects[0];
                    SQLHelper.RegisterToken(token);
                    saveToken(token.access_token);
                    CheckBox rememberPassword = findViewById(R.id.checkbox_remember_password);
                    if(rememberPassword.isChecked()){
                        Global.preference.put( LoginActivity.this, PrefConst.USERNAME, email);
                        Global.preference.put( LoginActivity.this, PrefConst.PASSWORD, password);
                    }else {
                        Global.preference.put( LoginActivity.this, PrefConst.USERNAME, "");
                        Global.preference.put( LoginActivity.this, PrefConst.PASSWORD, "");
                    }
                }
            }
        };
        this.loginToAPI(email, password, callBackHelper);
//        if (!getUser(email, password)) return;
    }

    private void saveToken(String token) {

        if (Global.TOKEN == null || Global.TOKEN.compareTo(token) != 0) {
            Global.preference.put(this, PrefConst.TOKEN, token);
            Global.TOKEN = token;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
                userId = user.getUid();
                DBUtils.AddNewUser(userId, user.getDisplayName(), user.getEmail());
                Global.preference.put(this, "userid", userId);

                Global.is_loggedin = true;
                LoadActivity();
                finish();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private void InitializeAuthentication(){
// Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
//        FirebaseAuth.getInstance().addAuthStateListener(onActivityResult);
    }
}
  