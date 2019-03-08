package com.puurva.findmetoo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.model.ActivityNotification;
import com.puurva.findmetoo.model.CurrentActivity;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.GPSTracker;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.puurva.findmetoo.ServiceInterfaces.*;
import com.puurva.findmetoo.uitls.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.*;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        FloatingSearchView.OnMenuItemClickListener,
        FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnHomeActionClickListener {

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
    private TextView viewProfile;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//       if(Global.preference == null || Global.preference.getValue(this, PrefConst.USERNAME, null) == null){
//           Intent loginIntent = new Intent(this, LoginActivity.class);
//           loginIntent.putExtra("activity", (Parcelable) getIntent());
//           loginIntent.putExtras(getIntent().getExtras());
//           startActivity(loginIntent);
//       }
    SetupFirebaseNotifications();

        StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(newbuilder.build());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);

        mSearchView = findViewById(R.id.floating_search_view);
        mSearchView.setOnMenuItemClickListener(this);
        mSearchView.setOnSearchListener(this);
        mSearchView.setOnHomeActionClickListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }
    }

    private void SetupFirebaseNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

//        FirebaseMessaging.getInstance().subscribeToTopic("weather")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
////                        String msg = getString(R.string.msg_subscribed);
//                        if (!task.isSuccessful()) {
////                            msg = getString(R.string.msg_subscribe_failed);
//                        }
////                        Log.d(TAG, msg);
////                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
////                            Log.w(TAG, "getInstanceId failed", task.getException());
//                            return;
//                        }
//
//                        // Get new Instance ID token
//                        String token = task.getResult().getToken();
//
//                        // Log and toast
////                        String msg = getString(R.string.msg_token_fmt, token);
////                        Log.d(TAG, msg);
////                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
//        if(Global.preference == null || Global.preference.getValue(this, PrefConst.USERNAME, null) == null){
//            Intent loginIntent = new Intent(this, LoginActivity.class);
//            loginIntent.putExtra("activity", (Parcelable) getIntent());
//            startActivity(loginIntent);
//        }

        mMap = googleMap;
        showMarkerOfUsers("");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Global.current_user.getLatitude(), Global.current_user.getLongitude()), 12));
        final MapWrapperLayout mapWrapperLayout = findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 20));

        this.imIn = mWindow.findViewById(R.id.iammin);
        this.infoImage = mWindow.findViewById(R.id.info_badge);
        this.viewProfile = mWindow.findViewById(R.id.view_profile_link);
        SendNotification();
        this.imIn.setOnTouchListener(infoButtonListener);

        this.infoImageButtonListener = new OnInfoWindowElemTouchListener(this.infoImage, null, null)
//                getResources().getDrawable(R.drawable.badge_sa), //btn_default_normal_holo_light
//                getResources().getDrawable(R.drawable.badge_sa)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
//                Toast.makeText(MapsActivity.this, marker.getTitle() + "'s image button clicked!", Toast.LENGTH_SHORT).show();
                LaunchImageViewer(v);
            }
        };
        this.infoImage.setOnTouchListener(infoImageButtonListener);

        this.viewProfileClickistener = new OnInfoWindowElemTouchListener(this.viewProfile, null, null)
//                getResources().getDrawable(R.drawable.badge_sa), //btn_default_normal_holo_light
//                getResources().getDrawable(R.drawable.badge_sa)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
//                Toast.makeText(MapsActivity.this, marker.getTitle() + "'s image button clicked!", Toast.LENGTH_SHORT).show();
                LaunchViewProfile(activities.get(marker).DeviceId);
            }
        };
        this.viewProfile.setOnTouchListener(viewProfileClickistener);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
//                double angle = 0.0;
//                double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 0.5;
//                double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 100.5);
//                marker.setInfoWindowAnchor((float)x, (float)y);
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
//            Toast.makeText(MapsActivity.this, "getInfoContents Called", Toast.LENGTH_SHORT).show();
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
                if (image.getTag(R.id.info_badge) == "loaded") {
                    image.setTag(R.id.info_badge, "DontReload");
                }
                return mWindow;
            }
        });

//        String deviceID = getIntent().getStringExtra("DeviceID");
//        String activityId = getIntent().getStringExtra("ActivityID");
//        NotificationType notificationType = (NotificationType) getIntent().getSerializableExtra("source");
//        RequestStatus requestStatus = (RequestStatus) getIntent().getSerializableExtra("RequestStatus");

        HandleNotifications();
    }

    private void HandleNotifications() {
        Log.e("ActivityNotification", "checking activity notification status in Maps Activity");
        activityNotification = getIntent().getParcelableExtra("ActivityNotification");
        if(activityNotification != null){
//            Toast.makeText(this, activityNotification.ActivityRequestStatus.name(), Toast.LENGTH_SHORT);
            Log.e("ActivityNotification", "activityNotification not null");
            switch (activityNotification.ActivityRequestStatus){
                case ACCEPTED:
                case REJECTED:
                    HandleFromNotification(activityNotification.ActivityId);
                    break;
                case NEW:
                    LaunchViewProfile(null);
            }
        }else{
            Log.e("ActivityNotification", "activityNotification is null");

        }
    }

    private void HandleFromNotification(String activityId) {
        String token = getToken();
        final String deviceId = Global.AndroidID;
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        try {
            Call<CurrentActivity> call = apiService.getActivityById("Bearer " + token, activityId);

            call.enqueue(new Callback<CurrentActivity>() {
                @Override
                public void onResponse(Call<CurrentActivity> call, Response<CurrentActivity> response) {
                    if (response.isSuccessful()) {
                        final CurrentActivity location = (CurrentActivity) (response.body());
                        Marker marker = ShowLocation(0, location, true);
                        marker.showInfoWindow();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 11));
                        marker.setTag(location.ImagePath);
                        marker.setVisible(true);
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
//                getResources().getDrawable(R.drawable.btn_bg), //btn_default_normal_holo_light
//                getResources().getDrawable(R.drawable.btn_bg)) //btn_default_pressed_holo_light
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
                } catch (Exception ex)
                {
                    Log.e("postActivity", ex.getMessage());
                }
                // Here we can perform some action triggered after clicking the button
            }
        };
    }

    @Override
    public void onActionMenuItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_search) {
            showMarkerOfUsers(mSearchView.getQuery());
        } else if (id == R.id.action_add) {
            addKeyword();
        } else if (id == R.id.menu_profile) {
            LoadProfileActivity(null);
        }

    }

    private void LoadProfileActivity(Intent intent) {
        if(intent == null) {
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
        this.bitmap=null;
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
            String filePath = GetImageFileFullPath();
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
                        bitmap = ImageUtility.scaleImageToResolution(this, this.bitmap, 300, 200, file);
                    }
                }
                break;
                case GALLERY_ACTIVITY: {
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                        bitmap = ImageUtility.scaleImageToResolution(this, this.bitmap, 300, 300, file);
                    }
                }
                break;
            }

            if (image != null && bitmap != null) {
//                bitmap = ImageUtility.scaleImageToResolution(this, this.bitmap, image.getWidth(), image.getWidth());
                image.setImageBitmap(bitmap);
            }

        }catch (Exception ex){
            Log.e("imageselector", ex.getMessage());
        }
    }

    private String getToken(String username) {
        String token = null;
        Cursor c = Global.mdb.rawQuery(
                "SELECT *    " +
                        "FROM apiuser " +
                        "WHERE deviceid = '" + username + "' " +
                        "LIMIT 1",
                null);

        if (c == null || c.getCount() == 0) {
            Global.showShortToast(this, "Api User not found!");
            finish();
        } else {
            c.moveToFirst();
            token = c.getString(2);
        }
        return token;
    }

    private void LaunchViewProfile(String deviceID) {
        Intent profileIntent = new Intent(this, ProfileViewActivity.class);
        if(deviceID != null) {
            profileIntent.putExtra("DeviceID", deviceID);
        }
        if(activityNotification != null)
        {
            profileIntent.putExtra("ActivityNotification", activityNotification);
            Log.e("ActivityNotification", activityNotification.ActivityRequestStatus.toString());
        }
        startActivity(profileIntent);
    }

    private void LaunchImageViewer(View v) {
        Intent viewImageIntent = new Intent(this, ViewImageActivity.class);
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ((BitmapDrawable)((ImageButton)v).getDrawable()).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] byteArray = bStream.toByteArray();
        viewImageIntent.putExtra("bitmap", byteArray);
        startActivity(viewImageIntent);
    }

    private void render(final Marker marker, View view) throws IOException {
        String title = marker.getTitle();
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
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }

        String token = getToken();
//        final Marker tempMarker = marker;
        if(marker.getTag() != "downloaded" && marker.getTag() != "") {
            GetActivityImage(marker, token);
        }
    }

    private String getToken() {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (token == null || token == "") {
            token = getToken(username);
        }
        return token;
    }

    private void GetActivityImage(final Marker marker, String token) {
        String deviceId = marker.getTag().toString().split("\\\\")[0];
        String fileName = marker.getTag().toString().split("\\\\")[1];
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.getMatchingImages("Bearer " + token, deviceId, fileName);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {

                    Log.d("onResponse", "Response came from server");

                    boolean FileDownloaded = false;
                    if (response.body() != null && (marker.getTag() == null || marker.getTag() != "downloaded")) {
                        FileDownloaded = DownloadImage(response.body());
                        marker.showInfoWindow();
                        marker.setTag("downloaded");
                    }
                    Log.d("onResponse", "Image is downloaded and saved ? " + FileDownloaded);

                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }

    private boolean DownloadImage(ResponseBody body) {

        try {
            Log.d("DownloadImage", "Reading and writing file");
            if (body != null) {
                // display the image data in a ImageView or save it
                Bitmap bmp = BitmapFactory.decodeStream(body.byteStream());

                int width, height;
                ImageButton image1 = ((ImageButton) mWindow.findViewById(R.id.info_badge));
                bmp = ImageUtility.scaleImageToResolution(this, bmp, bmp.getHeight(), bmp.getWidth());
                image1.setMaxWidth(bmp.getWidth());
                image1.setMaxHeight(bmp.getHeight());
                image1.setImageBitmap(bmp);
            }
            return true;

        } catch (Exception e) {
            Log.d("DownloadImage", e.toString());
            return false;
        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void showMarkerOfUsers(String search_text) {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        mMap.clear();
        // all markers remove
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

        search_text = search_text.toLowerCase();

        String token = getToken();

        GetMatchingActivitiesByKeyword(search_text, token);

    }

    private void GetMatchingActivitiesByKeyword(String search_text, String token) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<List<CurrentActivity>> call = apiService.getMatchingActivities("Bearer " + token, search_text);
        call.enqueue(new Callback<List<CurrentActivity>>() {
            @Override
            public void onResponse(Call<List<CurrentActivity>> call, Response<List<CurrentActivity>> response) {
                if (response.isSuccessful()) {
                    final Object[] locations = (response.body().toArray());
                    for (int i = 0; i < locations.length; i++) {
                        CurrentActivity location = (CurrentActivity) locations[i];
                        ShowLocation(i, location, false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CurrentActivity>> call, Throwable t) {
            }
        });
    }

    private Marker ShowLocation(int i, CurrentActivity location, boolean showFineLocation) {
        final String imagePath = location.ImagePath;
        LatLng pos = new LatLng(location.latitude, location.longitude);
        final Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("custom_info_bubble", 1, 1)))
                .snippet(location.description)
                .title(location.Activity));
        markers.put(i, marker);
        activities.put(marker, location);
        marker.setTag(location.ImagePath);
        if(!showFineLocation) {
            marker.setVisible(false);
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(500)
                    .clickable(true)
                    .strokeColor(Color.MAGENTA)
                    .fillColor(0x220000FF));
            double angle = 0.0;
            double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 0.5;
            double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 0.5);
//                marker.setInfoWindowAnchor((float)x, (float)(y+circle.getRadius()));
            if (i == 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(circle.getCenter(), 11));
            }
            mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

                @Override
                public void onCircleClick(Circle circle) {
                    Marker marker = markers.get(circles.get(circle));
                    marker.setVisible(true);
                    marker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(circle.getCenter(), 11));
                    marker.setTag(imagePath);
                }
            });
            circles.put(circle, i);
        }else{
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker selectedMarker) {
                    selectedMarker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedMarker.getPosition(), 11));
                    selectedMarker.setTag(imagePath);
                    return true;
                }
            });
        }

        return marker;
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void addKeyword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        view = LayoutInflater.from(this).inflate(R.layout.dialog_keyword, null);
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
                    File imageFile = new File(GetImageFileFullPath());
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

        ShowAddActivityDialog(builder, edit_title, edit_description, token);
    }

    private void ShowAddActivityDialog(AlertDialog.Builder builder, final EditText edit_title, final EditText edit_description, final String token) {
        builder.setTitle("Add Activity")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        CreateActivity(edit_description, edit_title, token);
                    }
                })
                .setNegativeButton("Cancel", null);
        builder.show();
    }

    private void CreateActivity(EditText edit_description, EditText edit_title, final String token) {
        final String deviceId = Global.AndroidID;
        String description = edit_description.getText().toString().trim();
        String title = edit_title.getText().toString().trim();
        if (title.isEmpty()) {
            Global.showShortToast(MapsActivity.this, "activity is required.");
            edit_title.requestFocus();
            return;
        }

        GPSTracker gpsTracker = new GPSTracker(MapsActivity.this);
        gpsTracker.getLocation();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        final ActivityModel activity = new ActivityModel(deviceId, title, description, currentDateandTime, gpsTracker.latitude, gpsTracker.longitude);

        PostNewActivity(token, deviceId, activity);
    }

    private void PostNewActivity(final String token, final String deviceId, final ActivityModel activity) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.postActivity("Bearer " + token, activity);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    uploadImage(deviceId, activity.What, token);
                    Log.e("PostActivity", "success returned");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("PostActivity", "Error on postActivity" + t.getMessage());
            }
        });
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

    private void uploadImage(String deviceId, String activity, String token) {
        try {
            String tempFileName = GetImageFileFullPath();
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
                out.flush();
                out.close();

            }
        } catch (Exception ex) {
            Log.e("ActivityImageUpload", ex.getMessage());
        }
    }

    private String GetImageFileFullPath() {
        try {
            File fileDir = new File(infoWindowImagePath);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
        }catch (Exception ex){
            Log.e("CreateDir", ex.getMessage(), ex);
        }
        return infoWindowImagePath + "/tempImagefile.jpeg";
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
