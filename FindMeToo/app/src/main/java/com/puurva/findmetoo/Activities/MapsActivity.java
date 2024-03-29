package com.puurva.findmetoo.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
//import android.support.annotation.NonNull;
//import android.support.annotation.RequiresApi;
//import android.support.design.widget.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
//import androidx.core.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.puurva.findmetoo.Enums.ActivityStatuses;
import com.puurva.findmetoo.Enums.ActivityTypes;
import com.puurva.findmetoo.Enums.ListViewTypes;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityNotification;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivitySettingsModel;
import com.puurva.findmetoo.ServiceInterfaces.model.CurrentActivity;
import com.puurva.findmetoo.ServiceInterfaces.model.NotificationDetails;
import com.puurva.findmetoo.ServiceInterfaces.model.NotificationRequestModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.Global;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.puurva.findmetoo.ServiceInterfaces.*;
import com.puurva.findmetoo.uitls.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.*;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        FloatingSearchView.OnMenuItemClickListener,
        FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnHomeActionClickListener {

    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation = null;
    public static final int CAMERA_ACTIVITY = 1;
    public static final int GALLERY_ACTIVITY = 0;
    private GoogleMap mMap;
    private HashMap<Integer, Marker> markers;
    private HashMap<Circle, Integer> circles;
    private HashMap<Marker, CurrentActivity> activities;
    private FloatingSearchView mSearchView;
    private View mWindow;
    View view = null;
    ImageView image = null;


//        private final View mContents;

    private Button imIn;
    private ImageButton infoImage;
    private Button viewProfile;

    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoImageButtonListener;
    private OnInfoWindowElemTouchListener viewProfileClickistener;

    private ViewTreeObserver.OnGlobalLayoutListener infoWindowLayoutListener;

    private MapWrapperLayout mapWrapperLayout;
    private Bitmap bitmap = null;
    private final String infoWindowImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Global.FILE_PATH_SUFFIX;
    Uri capturedImageUri = null;

    private String imageFilePath;
    private String imageFileName;
    private ActivityNotification activityNotification = null;
    private String notificationId = null;

    private boolean RELOADACTIVITIES = true;
//    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    PopupWindow mPopupWindow;
    PopupWindow mapLongClickPopupWindow;
    Marker newActivityMarker;
    private String activityIdOfNotification = null;
    private BottomNavigationView navView = null;
    private boolean isMapLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SetupFirebaseNotifications();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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

        StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(newbuilder.build());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, (ViewGroup) findViewById(R.id.info_window_main));
//        LinearLayout mainInfoWindowLayout = mWindow.findViewById(R.id.info_window_main);
//        mainInfoWindowLayout.getLayoutParams().height=200;

        mSearchView = findViewById(R.id.floating_search_view);
        mSearchView.setOnMenuItemClickListener(this);
        mSearchView.setOnSearchListener(this);
        mSearchView.setOnHomeActionClickListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setItemIconTintList(null);
//        DockBottomNavigation();
    }

    private void DockBottomNavigation() {

        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_view_my_activities:
                    LoadMyActivities();
                    return true;
                case R.id.action_add:
                    addActivity(null);
                    return true;
                case R.id.activity_notifications:
                    LoadMyNotiifications();
                    MenuItem notificationsItem = navView.getMenu().getItem(2);
                    notificationsItem.setIcon(R.drawable.activity_notifications_red);
                    return true;
            }
            return false;
        }
    };

    private void SetupFirebaseNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LoadMap(googleMap);
        GetCurrentLocation();
        this.isMapLoaded = true;
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        if(this.isMapLoaded) {
            LoadMap(mMap);
            GetCurrentLocation();
        }
        super.onResume();
    }


    private void LoadMap(GoogleMap googleMap) {
        mMap = googleMap;

        SetMapLongClickListener();

        final MapWrapperLayout mapWrapperLayout = findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 20));

        this.imIn = mWindow.findViewById(R.id.iammin);
        this.infoImage = mWindow.findViewById(R.id.info_badge);
        this.infoImage.setBackgroundResource(0);
        this.infoImage.setImageResource(0);

        this.viewProfile = mWindow.findViewById(R.id.view_profile_link);
        SendNotification();
        this.imIn.setOnTouchListener(infoButtonListener);

        this.infoImageButtonListener = new OnInfoWindowElemTouchListener(this.infoImage, null, null)
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                LaunchImageViewer(v);
            }
        };
        this.infoImage.setOnTouchListener(infoImageButtonListener);

        this.infoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                LaunchImageViewer(view);
            }
        });

        this.viewProfileClickistener = new OnInfoWindowElemTouchListener(this.viewProfile, null, null)
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                try {
                    LaunchViewProfile(activities.get(marker).DeviceId, false);
                } catch (Exception ex) {
//                    LaunchViewProfile(circles.get(marker) activities.get(marker).DeviceId, false);
                    Log.e("ShowProfile", ex.getMessage());
                }
            }
        };
        this.viewProfile.setOnTouchListener(viewProfileClickistener);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                ImageButton image = mWindow.findViewById(R.id.info_badge);
                try {
                    render(marker, mWindow);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                infoButtonListener.setMarker(marker);
                infoImageButtonListener.setMarker(marker);
                viewProfileClickistener.setMarker(marker);
                mapWrapperLayout.setMarkerWithInfoWindow(marker, mWindow);
                return mWindow;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (newActivityMarker != null) {
                    newActivityMarker.remove();
                }
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    try {
                        mPopupWindow.dismiss();
                        Circle circle = (Circle) mWindow.getTag();
                        if (circle != null) {
                            circle.setFillColor(570425599);
                        }
                    } catch (Exception ex) {
                        Log.e("mapclick", ex.getMessage());
                    }
                }
                if (mapLongClickPopupWindow != null && mapLongClickPopupWindow.isShowing()) {
                    try {
                        mapLongClickPopupWindow.dismiss();
                    } catch (Exception ex) {
                        Log.e("mapclick", ex.getMessage());
                    }
                }
                Global.ISMARKERCLICKED = false;
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    if(!Global.ISMARKERCLICKED) {
                        RELOADACTIVITIES = true;
                    }
                }
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (RELOADACTIVITIES) {
                    showMarkerOfUsers(mSearchView.getQuery());
                    Log.e("CameraIdle", mSearchView.getQuery());
                    RELOADACTIVITIES = false;
                }
                if(mPopupWindow != null && mPopupWindow.isShowing()){
                    mPopupWindow.dismiss();
                }
            }
        });

        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnCameraChangeListener(getCameraChangeListener());

//        GetCurrentLocation();
        HandleNotifications();
//        mMap.getUiSettings().setMapToolbarEnabled(true);
        GetNotifications(Global.AndroidID);

        setInfoWindowOnClick();
    }

    private void GetCurrentLocation() {
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
                        if (location != null && !Global.ISMARKERCLICKED) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
//                            Global.ISMARKERCLICKED = false;
                            // Logic to handle location object
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String test = e.getMessage();
                    }
                });
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

    private void setInfoWindowOnClick(){
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                ShowActivityInFullView(marker);
            }
        });
    }

    private void GetNotifications(String deviceId) {
        final String token = getToken();
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<List<com.puurva.findmetoo.ServiceInterfaces.model.NotificationDetails>> call = apiService.geMyNotifications("Bearer " + token, deviceId);
        try {
            call.enqueue(new Callback<List<com.puurva.findmetoo.ServiceInterfaces.model.NotificationDetails>>() {
                @Override
                public void onResponse(Call<List<com.puurva.findmetoo.ServiceInterfaces.model.NotificationDetails>> call, Response<List<com.puurva.findmetoo.ServiceInterfaces.model.NotificationDetails>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().toArray().length > 0) {
                            MenuItem notificationsItem = navView.getMenu().getItem(2);
                            notificationsItem.setIcon(R.drawable.activity_notifications_pending);
                        } else {
                            MenuItem notificationsItem = navView.getMenu().getItem(2);
                            notificationsItem.setIcon(R.drawable.activity_notifications_red);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<NotificationDetails>> call, Throwable t) {
                }
            });
        } catch (Exception ex) {
            Log.e("ReviewsDownload", ex.getMessage());
        }
    }

    private void SetMapLongClickListener() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                Global.ISMARKERCLICKED = false;
                newActivityMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(17)
                        .bearing(0)
                        .tilt(0)
                        .build();

                View addActivityConfirmPopupLayout = getLayoutInflater().inflate(R.layout.confirm_add_activity, (ViewGroup) findViewById(R.id.confirm_add_dialog_top));
                TextView confirmAddActivityTextView = addActivityConfirmPopupLayout.findViewById(R.id.confirm_add_activity_text);
                final Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geo.getFromLocation(newActivityMarker.getPosition().latitude, newActivityMarker.getPosition().longitude, 1);
                    if (!addresses.isEmpty() && addresses.size() > 0) {
                        confirmAddActivityTextView.setText(confirmAddActivityTextView.getText() + " at : " + addresses.get(0).getAddressLine(0) + "?");
//                            confirmAddActivityTextView.setText(confirmAddActivityTextView.getText() + " at : " + addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName() + "?");
                        //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                        String timezoneLocation = addresses.get(0).getCountryName() + "\\" + addresses.get(0).getLocality();
                    } else {
                        confirmAddActivityTextView.setText(confirmAddActivityTextView.getText() + "?");
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // getFromLocation() may sometimes fail
                }

                mapLongClickPopupWindow = new PopupWindow(
                        addActivityConfirmPopupLayout,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                mapLongClickPopupWindow.setFocusable(true);
                if (Build.VERSION.SDK_INT >= 21) {
                    mapLongClickPopupWindow.setElevation(5.0f);
                }
                RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.maps_layout);
                mapLongClickPopupWindow.showAtLocation(mRelativeLayout, Gravity.BOTTOM, 0, 0);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                Button confirmAddActivityYes = (Button) addActivityConfirmPopupLayout.findViewById(R.id.confirm_add_activity_yes);
                Button confirmAddActivityNo = (Button) addActivityConfirmPopupLayout.findViewById(R.id.confirm_add_activity_no);
                confirmAddActivityYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        addActivity(latLng);
                        newActivityMarker.remove();
                        mapLongClickPopupWindow.dismiss();
                    }
                });

                confirmAddActivityNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        newActivityMarker.remove();
                        mapLongClickPopupWindow.dismiss();
                    }
                });
                mapLongClickPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        if (newActivityMarker != null) {
                            newActivityMarker.remove();
                        }
                    }
                });
//                mMap.setPadding(0,0,0, 150 );
            }
        });
    }

    private void HandleNotifications() {
        Log.e("ActivityNotification", "checking activity notification status in Maps Activity");
        activityNotification = getIntent().getParcelableExtra("ActivityNotification");
        activityIdOfNotification = getIntent().getStringExtra("ActivityIdOfNotification");
        notificationId = getIntent().getStringExtra("NotificationId");
        getIntent().removeExtra("ActivityNotification");
        if (activityNotification != null) {
//            Toast.makeText(this, activityNotification.ActivityRequestStatus.name(), Toast.LENGTH_SHORT);
            Log.e("ActivityNotification", "activityNotification not null");
            switch (activityNotification.ActivityRequestStatus) {
                case ACCEPTED:
                    HandleFromNotification(activityNotification.ActivityId, notificationId);
                    break;
                case NEW:
                    LaunchViewProfile(activityNotification.DeviceId, true);
                    break;
                case REJECTED:
                    showMarkerOfUsers("");
                    if (notificationId != null) {
                        DeleteNotification(notificationId);
                    }
                    break;
            }
        } else if (activityIdOfNotification != null) {
            HandleFromNotification(activityIdOfNotification, notificationId);
//            if(notificationId != null){
////                DeleteNotification(notificationId);
//            }
        } else {
            showMarkerOfUsers("");
            Log.e("ActivityNotification", "activityNotification is null");
        }
        getIntent().removeExtra("ActivityNotification");
        getIntent().removeExtra("ActivityIdOfNotification");
        getIntent().removeExtra("NotificationId");
    }

    private void DeleteNotification(String notificationId) {
        String token = getToken();
        final String deviceId = Global.AndroidID;
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        try {
            InitializeHashMaps();
            Call<Void> call = apiService.deleteNotification("Bearer " + token, notificationId);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("onFailure", t.toString());
                }
            });
        } catch (Exception ex) {
            Log.e("postActivity", ex.getMessage());
        }
    }

    private void HandleFromNotification(String activityId, final String notificationId) {
        String token = getToken();
        final String deviceId = Global.AndroidID;
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        try {
            InitializeHashMaps();
            Call<CurrentActivity> call = apiService.getActivityById("Bearer " + token, activityId);

            call.enqueue(new Callback<CurrentActivity>() {
                @Override
                public void onResponse(Call<CurrentActivity> call, Response<CurrentActivity> response) {
                    if (response.isSuccessful()) {
                        final CurrentActivity location = (CurrentActivity) (response.body());
//                        mMap.clear();
                        Marker marker = ShowLocation(0, location, true);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
                        marker.setTag(location.ImagePath);
                        marker.setVisible(true);
                        marker.showInfoWindow();
                        if (notificationId != null) {
                            DeleteNotification(notificationId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<CurrentActivity> call, Throwable t) {
                    Log.d("onFailure", t.toString());
                }
            });
        } catch (Exception ex) {
            Log.e("postActivity", ex.getMessage());
        }
    }

    private void SendNotification() {
        this.infoButtonListener = new OnInfoWindowElemTouchListener(this.imIn, null, null)
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                CurrentActivity activity = activities.get(marker);
                String activityId = activity.ActivityId;
                String token = getToken();
                final String deviceId = Global.AndroidID;
                ApiInterface apiService =
                        HttpClient.getClient().create(ApiInterface.class);
                try {
                    NotificationRequestModel notificationRequestModel = new NotificationRequestModel(deviceId, activity.DeviceId, NotificationType.AMIN, activityId, RequestStatus.NEW);
                    Call<Void> call = apiService.sendNotification("Bearer " + token, notificationRequestModel);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MapsActivity.this, "Your request has been submitted. We will let you know when accepted.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.d("onFailure", t.toString());
                        }
                    });
                } catch (Exception ex) {
                    Log.e("postActivity", ex.getMessage());
                }
                // Here we can perform some action triggered after clicking the button
            }
        };
    }

    @Override
    public void onActionMenuItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                showMarkerOfUsers(mSearchView.getQuery());
                break;
            case R.id.menu_profile:
                LoadProfileActivity(null);
                break;
            case R.id.menu_profile_reviews:
                LoadProfileReviews();
                break;
        }
    }

    private void LoadMyRequests() {
        try {
            Intent myRequestsIntent = new Intent(this, Requests.class);
            myRequestsIntent.putExtra("DeviceId", CommonUtility.GetDeviceId());
            myRequestsIntent.putExtra("ListSource", ListViewTypes.MYREQUEASTS);
            startActivity(myRequestsIntent);
        } catch (Exception ex) {
            Log.e("LoadProfileViews", ex.getMessage());
        }
    }

    private void LoadProfileReviews() {
        try {
            Intent profileReviewsIntent = new Intent(this, ViewListActivity.class);
            profileReviewsIntent.putExtra("DeviceId", CommonUtility.GetDeviceId());
            profileReviewsIntent.putExtra("ListSource", ListViewTypes.PROFILEREVIEWS);
            startActivity(profileReviewsIntent);
        } catch (Exception ex) {
            Log.e("LoadProfileViews", ex.getMessage());
        }
    }

    private void LoadMyActivities() {
        try {
            Intent myActivitiesIntent = new Intent(this, ViewListActivity.class);
            myActivitiesIntent.putExtra("DeviceId", CommonUtility.GetDeviceId());
            myActivitiesIntent.putExtra("ListSource", ListViewTypes.MYACTIVITIES);
            startActivity(myActivitiesIntent);
        } catch (Exception ex) {
            Log.e("LoadProfileViews", ex.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void LoadMyNotiifications() {
        try {
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.cancelAll();
            Intent myNotificationsIntent = new Intent(this, ViewNotificationsList.class);
            myNotificationsIntent.putExtra("DeviceId", CommonUtility.GetDeviceId());
            myNotificationsIntent.putExtra("ListSource", ListViewTypes.MYNOTIFICATIONS);
            startActivity(myNotificationsIntent);
        } catch (Exception ex) {
            Log.e("LoadMYNotifications", ex.getMessage());
        }
    }

    private void LoadProfileActivity(Intent intent) {
        if (intent == null) {
            intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("DeviceID", CommonUtility.GetDeviceId());
        }
        startActivity(intent);
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
    }

    @Override
    public void onSearchAction(String currentQuery) {
        this.bitmap = null;
        showMarkerOfUsers(currentQuery);
    }

    @Override
    public void onHomeClicked() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert")
                .setCancelable(false)
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .setNegativeButton("No", null);

        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            String filePath = CommonUtility.GetImageFileFullPath();
            File file = new File(filePath);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            switch (requestCode) {
                case CAMERA_ACTIVITY: {
                    if (resultCode == Activity.RESULT_OK) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        float angle = ImageUtility.getExifAngle(this, filePath);
                        bitmap = ImageUtility.rotateImage(filePath, angle);
                        bitmap = ImageUtility.scaleImageToResolution(this.bitmap, 300, 200, file);
                    }
                }
                break;
                case GALLERY_ACTIVITY: {
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                        bitmap = ImageUtility.scaleImageToResolution(this.bitmap, 300, 300, file);
                    }
                }
                break;
            }

            if (image != null && bitmap != null) {
//                bitmap = ImageUtility.scaleImageToResolution(this.bitmap, image.getWidth(), image.getWidth());
                image.setImageBitmap(bitmap);
            }

        } catch (Exception ex) {
            Log.e("imageselector", ex.getMessage());
        }
    }

    private void LaunchViewProfile(String deviceID, boolean isFromNotification) {
        Intent profileIntent = new Intent(this, ProfileViewActivity.class);
        if (deviceID != null) {
            profileIntent.putExtra("DeviceID", deviceID);
        }
        if (activityNotification != null && isFromNotification) {
            profileIntent.putExtra("ActivityNotification", activityNotification);
        }
        startActivity(profileIntent);

    }

    private void LaunchImageViewer(View v) {
        Drawable drawable = ((ImageButton) v).getDrawable();
        if (drawable != null) {
            Bitmap imageBitmap = ((BitmapDrawable) drawable).getBitmap();
            if (imageBitmap != null) {
                Intent viewImageIntent = new Intent(this, ViewImageActivity.class);
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
                byte[] byteArray = bStream.toByteArray();
                viewImageIntent.putExtra("bitmap", byteArray);
                startActivity(viewImageIntent);
            }
        }
    }

    private void render(final Marker marker, View view) throws IOException {
        String title = marker.getTitle();
        CurrentActivity activity = activities.get(marker);
        TextView titleUi = view.findViewById(R.id.title);
        if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString(title);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = view.findViewById(R.id.snippet);
        snippetUi.setSelected(true);
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }

        if (activity != null) {
            ((RatingBar) view.findViewById(R.id.profile_rating_indicator)).setRating(activity.ProfileRating);
            try {
            if(activity.ActivityStartTime != null) {
                TextView startTime = view.findViewById(R.id.start_time);
                Date startDateTime = null;
                    startDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(activity.ActivityStartTime);
                String formattedFromDate = Global.activityDateFormat.format(startDateTime);
                startTime.setText(formattedFromDate);
            }
            if(activity.ActivityStartTime != null) {
                TextView endTime = view.findViewById(R.id.end_time);
                Date endDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(activity.ActivityEndTime);
                String formattedToDate = Global.activityDateFormat.format(endDateTime);
                endTime.setText(formattedToDate);
            }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String token = getToken();
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.info_badge);
        if (marker.getTag() != "" && !ImageUtility.SetImage(CommonUtility.GetFilePath() + marker.getTag().toString().split("\\\\")[1] + ".png",
                imageButton, 200, 250)) {
            ImageUtility.GetActivityImage(marker.getTag().toString(), imageButton, token, 200, 250);
        } else if (marker.getTag() == "" || marker.getTag() == null) {
            ((ImageButton) view.findViewById(R.id.info_badge)).setImageResource(0);
        }

    }

    private String getToken() {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (token == null || token == "") {
            token = SQLHelper.getToken(username);
        }
        return token;
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void showMarkerOfUsers(String search_text) {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        mMap.clear();
        // all markers remove
        InitializeHashMaps();

        if (search_text != null) {
            search_text = search_text.toLowerCase();
        }

        String token = getToken();
        GetMatchingActivitiesByKeyword(search_text, token);
    }

    private void InitializeHashMaps() {
        if (markers != null) {
            markers.clear();
        } else {
            markers = new HashMap<Integer, Marker>();
        }

        if (circles != null) {
            circles.clear();
        } else {
            circles = new HashMap<Circle, Integer>();
        }

        if (activities != null) {
            activities.clear();
        } else {
            activities = new HashMap<Marker, CurrentActivity>();
        }
    }

    private void GetMatchingActivitiesByKeyword(String search_text, String token) {
        final VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        try {
            Call<List<CurrentActivity>> call;
            if (search_text != null && search_text.trim().length() > 0) {
                call = apiService.getMatchingActivities("Bearer " + token, Global.AndroidID, search_text, visibleRegion.farLeft.latitude,
                        visibleRegion.nearLeft.latitude, visibleRegion.farLeft.longitude, visibleRegion.farRight.longitude);
            } else {
                call = apiService.getAllActivities("Bearer " + token, Global.AndroidID, visibleRegion.farLeft.latitude,
                        visibleRegion.nearLeft.latitude, visibleRegion.farLeft.longitude, visibleRegion.farRight.longitude);
            }
            call.enqueue(new Callback<List<CurrentActivity>>() {
                @Override
                public void onResponse(Call<List<CurrentActivity>> call, Response<List<CurrentActivity>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final Object[] locations = (response.body().toArray());
                        for (int i = 0; i < locations.length; i++) {
                            CurrentActivity location = (CurrentActivity) locations[i];
                            boolean showFineLocation = ActivityTypes.valueOf(location.ActivityType) == ActivityTypes.ONREQUEST ? false : true;
                            if (showFineLocation == false) {
                                showFineLocation = RequestStatus.valueOf(location.ActivityRequestStatus) == RequestStatus.ACCEPTED ? true : false;
                            }
                            ShowLocation(i, location, showFineLocation);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<CurrentActivity>> call, Throwable t) {
                }
            });
        } catch (Exception ex) {
            Log.e("GetMatchingLocations", ex.getMessage());
        }
    }

    private float previousZoomLevel = -1.0f;

    private boolean isZooming = false;

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition position) {
//                Log.d("Zoom", "Zoom: " + position.zoom);

                if (previousZoomLevel != position.zoom) {
                    isZooming = true;
                }
                if (position.zoom < 14.0 && position.zoom > 0 && circles != null) {
                    for (Circle cir : circles.keySet()) {
                        cir.setRadius(200 * (int) (15 - position.zoom));
//                        cir.setRadius(cir.getRadius() + (int)(10 / (position.zoom / 50)));
                    }
                }
                previousZoomLevel = position.zoom;

//                Log.d("zoomLevel : ", String.valueOf(previousZoomLevel));
            }
        };
    }

    private Marker ShowLocation(int i, CurrentActivity location, final boolean showFineLocation) {
        final String imagePath = location.ImagePath;
        LatLng pos = new LatLng(location.Lat, location.Long);
        final Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker_icon", 80, 80)))
                .snippet(location.Description)
                .title(location.Activity));
        markers.put(i, marker);
        activities.put(marker, location);
        marker.setTag(imagePath);
        if (!showFineLocation) {
            LatLng alteredCirclePosition = CommonUtility.getAlteredLocation(pos, 40);
            marker.setVisible(false);
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(alteredCirclePosition)
                    .radius(200)
                    .clickable(true)
                    .strokeColor(Color.MAGENTA)
                    .fillColor(0x220000FF));
            mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

                @Override
                public void onCircleClick(Circle circle) {
                    Marker marker = markers.get(circles.get(circle));
                    marker.setVisible(false);
                    marker.hideInfoWindow();
                    ShowInfoPopup(circle, marker);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
                }

            });
            circles.put(circle, i);
        } else {
            marker.setVisible(true);
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker selectedMarker) {
                    if (mWindow.getParent() != null) {
                        ((ViewGroup) mWindow.getParent()).removeView(mWindow);
                    }
//                    selectedMarker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedMarker.getPosition(), 14));
                    return false;
                }
            });
        }

        return marker;
    }

    private void ShowInfoPopup(Circle circle, final Marker marker) {
        try {
            if (mWindow.getParent() != null) {
                ((ViewGroup) mWindow.getParent()).removeView(mWindow);
            }
            Circle oldCircle = (Circle) mWindow.getTag();
            if (oldCircle != null) {
                oldCircle.setFillColor(570425599);
            }
            render(marker, mWindow);
            mPopupWindow = new PopupWindow(
                    mWindow,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
//            mPopupWindow.setFocusable(true);
            if (Build.VERSION.SDK_INT >= 21) {
                mPopupWindow.setElevation(5.0f);
            }
            RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.maps_layout);
            final View.OnTouchListener activityOnClick = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    mPopupWindow.dismiss();
                    ShowActivityInFullView(marker);
                    return false;
                }
            };
            mPopupWindow.setTouchInterceptor(activityOnClick);
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.showAtLocation(mRelativeLayout, Gravity.BOTTOM, 0, 0);
            mWindow.setTag(circle);
            infoButtonListener.setMarker(marker);
            infoImageButtonListener.setMarker(marker);
            viewProfileClickistener.setMarker(marker);
            Button imIn = mWindow.findViewById(R.id.iammin);
            imIn.setVisibility(View.VISIBLE);
            circle.setFillColor(R.color.black);
//        mPopupWindow.getContentView().setOnClickListener(activityOnClick);
//            mWindow.findViewById(R.id.info_window_rating_bar).setOnClickListener(activityOnClick);
//            mWindow.findViewById(R.id.start_time).setOnClickListener(activityOnClick);
//            mWindow.findViewById(R.id.end_time).setOnClickListener(activityOnClick);
//            mWindow.findViewById(R.id.time_to).setOnClickListener(activityOnClick);
        } catch (Exception ex) {
            Log.e("popup", ex.getMessage());
        }
    }

    private void ShowActivityInFullView(Marker marker) {
        CurrentActivity activity = activities.get(marker);
        if (activity != null) {
            Intent viewFullActivity = new Intent(MapsActivity.this, ViewActivityFull.class);
            viewFullActivity.putExtra("Activity", activity);
            startActivity(viewFullActivity);
        }
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void addActivity(LatLng latLng) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        view = LayoutInflater.from(this).inflate(R.layout.dialog_keyword, null);
        LinearLayout editActivityLayout = view.findViewById(R.id.activity_edit_actions);
        editActivityLayout.setVisibility(View.GONE);
        final EditText edit_title = view.findViewById(R.id.edit_activity_title);
        final EditText edit_description = view.findViewById(R.id.edit_activity_description);
        image = view.findViewById(R.id.edit_activity_badge);
        image.setImageResource(R.drawable.takeaphoto);
        image.setClickable(true);
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File imageFile = new File(CommonUtility.GetImageFileFullPath());
                    capturedImageUri = Uri.fromFile(imageFile);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                    startActivityForResult(takePicture, CAMERA_ACTIVITY);
                } catch (Exception ex) {
                    Log.e("cameraintenterror", ex.getMessage());
                }
            }
        });

        Button btnPick = view.findViewById(R.id.button_pick);
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, GALLERY_ACTIVITY);
                } catch (Exception ex) {
                    Log.e("cameraintenterror", ex.getMessage());
                }
            }
        });

        final Button btnSettings = view.findViewById(R.id.btn_activity_settings);
        final LinearLayout activitySettings = view.findViewById(R.id.activity_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (btnSettings.getText().equals("SETTINGS")) {
                        btnSettings.setText("HIDE");
                        activitySettings.setVisibility(View.VISIBLE);
                    } else if (btnSettings.getText().equals("HIDE")) {
                        btnSettings.setText("SETTINGS");
                        activitySettings.setVisibility(View.GONE);
                    }
                } catch (Exception ex) {
                    Log.e("cameraintenterror", ex.getMessage());
                }
            }
        });

        final EditText startDate = (EditText) view.findViewById(R.id.activity_start_date);
        final EditText endDate = (EditText) view.findViewById(R.id.activity_end_date);
        final Switch isPrivate = (Switch) view.findViewById(R.id.activity_is_private);
//        startDate.setKeyListener(null);
//        endDate.setKeyListener(null);
        HandleMoreSettings(startDate, endDate, isPrivate);

        ShowAddActivityDialog(builder, edit_title, edit_description, startDate, endDate, isPrivate, token, latLng);
    }

    private void HandleMoreSettings(final EditText startDate, final EditText endDate, Switch isPrivate) {
        final Calendar startCalendar = Calendar.getInstance();
        final Calendar endCalendar = Calendar.getInstance();
        String formattedFromDate = Global.activityDateFormat.format(startCalendar.getTime());
        startDate.setText(formattedFromDate);
        startDate.setTag(Global.universalDateFormat.format(startCalendar.getTime()));
        endCalendar.add(Calendar.HOUR, 4);
        String formattedToDate = Global.activityDateFormat.format(endCalendar.getTime());
        endDate.setText(formattedToDate);
        endDate.setTag(Global.universalDateFormat.format(endCalendar.getTime()));

        Date value = new Date();
        startCalendar.setTime(value);
        final DatePickerDialog.OnDateSetListener startTimePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, monthOfYear);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // now show the time picker
                new TimePickerDialog(MapsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view,
                                                  int h, int min) {
                                startCalendar.set(Calendar.HOUR_OF_DAY, h);
                                startCalendar.set(Calendar.MINUTE, min);
//                                date = startCalendar.getTime();

                                startDate.setText(Global.activityDateFormat.format(startCalendar.getTime()));
                                startDate.setTag(Global.universalDateFormat.format(startCalendar.getTime()));
                            }
                        }, startCalendar.get(Calendar.HOUR_OF_DAY),
                        startCalendar.get(Calendar.MINUTE), false).show();

            }

        };

        final DatePickerDialog.OnDateSetListener endTimePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, monthOfYear);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // now show the time picker
                new TimePickerDialog(MapsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view,
                                                  int h, int min) {
                                endCalendar.set(Calendar.HOUR_OF_DAY, h);
                                endCalendar.set(Calendar.MINUTE, min);
//                                date = startCalendar.getTime();

                                endDate.setText(Global.activityDateFormat.format(endCalendar.getTime()));
                                endDate.setTag(Global.universalDateFormat.format(endCalendar.getTime()));
                            }
                        }, endCalendar.get(Calendar.HOUR_OF_DAY),
                        endCalendar.get(Calendar.MINUTE), false).show();

            }

        };
//            startDate.setOnClickListener(null);
//            endDate.setOnClickListener(null);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    if (hasFocus) {
                new DatePickerDialog(MapsActivity.this, startTimePicker, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)).show();
//                    }
            }
        });

        startDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    new DatePickerDialog(MapsActivity.this, startTimePicker, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                            startCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MapsActivity.this, endTimePicker, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) {
                    new DatePickerDialog(MapsActivity.this, endTimePicker, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                            endCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });


    }

    private void ShowAddActivityDialog(AlertDialog.Builder builder, final EditText edit_title, final EditText edit_description, final EditText startDate, final EditText endDate, final Switch isPrivate, final String token, final LatLng latLng) {
        builder.setTitle("Add Activity")
                .setView(view)
                .setCancelable(false)

                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        CreateActivity(edit_description, edit_title, startDate, endDate, isPrivate, token, latLng);
                    }
                })
                .setNegativeButton("Cancel", null);
        builder.show();
    }

    private void CreateActivity(EditText edit_description, EditText edit_title, final EditText startDate, final EditText endDate, final Switch isPrivate, final String token, final LatLng latLng) {
        final String deviceId = Global.AndroidID;
        final String description = edit_description.getText().toString().trim();
        final String title = edit_title.getText().toString().trim();
        if (title.isEmpty()) {
            Global.showShortToast(MapsActivity.this, "activity is required.");
            edit_title.requestFocus();
            return;
        }

//        GPSTracker gpsTracker = new GPSTracker(MapsActivity.this);
//        gpsTracker.getLocation();
        final String currentDateandTime = Global.universalDateFormat.format(new Date());
        if (latLng == null) {
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
                                ActivityModel activity = new ActivityModel(null, deviceId, title, description, currentDateandTime, location.getLatitude(), location.getLongitude());

                                ActivityTypes activityTypes = isPrivate.isChecked() ? ActivityTypes.ONREQUEST : ActivityTypes.PUBLIC;
                                final ActivitySettingsModel activitySettings = new ActivitySettingsModel(null, startDate.getTag().toString(), endDate.getTag().toString(), activityTypes, ActivityStatuses.OPEN, 0, 0, null);
                                activity.activitySetting = activitySettings;

                                PostNewActivity(token, deviceId, activity);
                                showMarkerOfUsers(mSearchView.getQuery());
                            }
                        }
                    });
        } else {
            ActivityModel activity = new ActivityModel(null, deviceId, title, description, currentDateandTime, latLng.latitude, latLng.longitude);

            ActivityTypes activityTypes = isPrivate.isChecked() ? ActivityTypes.ONREQUEST : ActivityTypes.PUBLIC;
            final ActivitySettingsModel activitySettings = new ActivitySettingsModel(null, startDate.getTag().toString(), endDate.getTag().toString(), activityTypes, ActivityStatuses.OPEN, 0, 0, null);
            activity.activitySetting = activitySettings;

            PostNewActivity(token, deviceId, activity);
        }
    }

    private void PostNewActivity(final String token, final String deviceId, final ActivityModel activity) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ActivityModel> call = apiService.postActivity("Bearer " + token, activity);
        call.enqueue(new Callback<ActivityModel>() {
            @Override
            public void onResponse(Call<ActivityModel> call, Response<ActivityModel> response) {
                if (response.isSuccessful()) {
                    ActivityModel newActivityModel = response.body();
                    if(newActivityModel != null) {
                        uploadImage(deviceId, newActivityModel.activitySetting.ActivityId, token);
                        Log.e("PostActivity", "success returned");
                        LoadMap(mMap);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(activity.Latitude, activity.Longitude), 14));
//                        finish();
//                        startActivity(getIntent());
//                        GetMatchingActivitiesByKeyword(token, activity.What);
                    }
                }
            }

            @Override
            public void onFailure(Call<ActivityModel> call, Throwable t) {
                Log.e("PostActivity", "Error on postActivity" + t.getMessage());
            }
        });
    }

    private void uploadImage(String deviceId, String activity, String token) {
        try {
            String tempFileName = CommonUtility.GetImageFileFullPath();
            if (this.bitmap != null) {
                FileOutputStream out = new FileOutputStream(tempFileName);
                this.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                File file = new File(tempFileName);
//            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), requestBody);

                RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

                PostActivityImage(deviceId, activity, token, body);
                this.bitmap = null;
                out.flush();
                out.close();

            }
        } catch (Exception ex) {
            Log.e("ActivityImageUpload", ex.getMessage());
        }
    }

    private void PostActivityImage(String deviceId, String activity, String token, MultipartBody.Part body) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.postActivityImage("Bearer " + token, deviceId, activity, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("imageupload", "success");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ActivityImageUploader", t.getMessage());
            }
        });
    }
}
