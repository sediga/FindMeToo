package com.bluesky.findmetoo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.bluesky.findmetoo.model.CurrentActivity;
import com.bluesky.findmetoo.preference.PrefConst;
import com.bluesky.findmetoo.uitls.GPSTracker;
import com.bluesky.findmetoo.uitls.Global;
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

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.bluesky.findmetoo.ServiceInterfaces.*;
import com.bluesky.findmetoo.uitls.*;
import retrofit2.Call;
import retrofit2.*;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        FloatingSearchView.OnMenuItemClickListener,
        FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnHomeActionClickListener {

    private GoogleMap mMap;
    private HashMap<Integer, Marker> markers;
    private HashMap<Circle, Integer> circles;
    private FloatingSearchView mSearchView;
    private Button infoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSearchView = findViewById(R.id.floating_search_view);
        mSearchView.setOnMenuItemClickListener(this);
        mSearchView.setOnSearchListener(this);
        mSearchView.setOnHomeActionClickListener(this);
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
        mMap = googleMap;
        showMarkerOfUsers("");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Global.current_user.getLatitude(), Global.current_user.getLongitude()), 12));
        //mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
//        mMap.setOnInfoWindowClickListener(this);
//        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
//
//        // MapWrapperLayout initialization
//        // 39 - default marker height
//        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
//        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    public void onActionMenuItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_search) {
            showMarkerOfUsers(mSearchView.getQuery());
        } else if (id == R.id.action_add) {
            addKeyword();
        }
        else if (id == R.id.menu_profile){
            startActivity(new Intent(this, ProfileActivity.class));
        }

    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {}

    @Override
    public void onSearchAction(String currentQuery) {
        showMarkerOfUsers(currentQuery);
    }

    @Override
    public void onHomeClicked() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert")
                .setCancelable(false)
                .setMessage("Are you sure finish?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null);

        builder.show();
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

    private void showMarkerOfUsers(String search_text) {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        mMap.clear();
        // all markers remove
        if (markers != null) {
            markers.clear();
//            for (Marker marker : markers) marker.remove();
        }
        else {
            markers = new HashMap<Integer, Marker>();
        }

        if (circles != null) {
            circles.clear();
//            for (Circle circle : circles) circle.remove();
        }
        else {
            circles = new HashMap<Circle, Integer>();
        }

        search_text = search_text.toLowerCase();

        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (token == null || token == "") {
            token = getToken(username);
        }

        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<List<CurrentActivity>> call = apiService.getMatchingActivities("Bearer " + token, search_text);
        call.enqueue(new Callback<List<CurrentActivity>>() {
            @Override
            public void onResponse(Call<List<CurrentActivity>> call, Response<List<CurrentActivity>> response) {
                if(response.isSuccessful()) {
                    Object[] locations = (response.body().toArray());
                    for (int i = 0; i < locations.length; i++) {
                        LatLng pos = new LatLng(((CurrentActivity) locations[i]).latitude, ((CurrentActivity) locations[i]).longitude);
                        final Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(pos).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("custom_info_bubble",1,1)))
                                .visible(false)
                                .snippet(((CurrentActivity) locations[i]).description)
                                .title(((CurrentActivity) locations[i]).activity));
                        markers.put(i, marker);
                        Circle circle = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(((CurrentActivity) locations[i]).latitude, ((CurrentActivity) locations[i]).longitude))
                                .radius(500)
                                .clickable(true)
                                .strokeColor(Color.MAGENTA)
                                .fillColor(0x220000FF));

                        if(i==0) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(circle.getCenter(), 11));
                        }

                        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

                            @Override
                            public void onCircleClick(Circle circle) {
                                Marker marker = markers.get(circles.get(circle));
                                marker.setVisible(true);
                                marker.showInfoWindow();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(circle.getCenter(), 11));
//                                marker.setVisible(false);
                                //infoWindow.setVisibility(View.VISIBLE);
                                circle.setTag("test");
                                // Flip the r, g and b components of the circle's
                                // stroke color.
//                                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
//                                circle.setStrokeColor(strokeColor);

                             }
                        });
                            circles.put(circle, i);
                    }
                }
            }

            public Bitmap resizeMapIcons(String iconName, int width, int height){
                Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
                return resizedBitmap;
            }

            @Override
            public void onFailure(Call<List<CurrentActivity>> call, Throwable t) {
            }
        });


//        int count = c.getCount();
//        markers = new ArrayList<>();
//
//        boolean is_contain_current_user = false;
//        double average_lat = 0, average_lng = 0;
//
//        for (int i = 0; i < count; i++) {
//
//            c.moveToPosition(i);
//            Cursor c1 = Global.mdb.rawQuery("SELECT * FROM t_keyword WHERE uid = " + c.getInt(0), null);
//
//            int count1 = c1.getCount();
//
//            if (c.getString(1).toLowerCase().contains(search_text) ||
//                    c.getString(2).toLowerCase().contains(search_text) ||
//                    search_text.isEmpty()) {
//
//                if (c.getInt(0) == Global.current_user.getId()) is_contain_current_user = true;
//                String full_name = String.format("%s %s", c.getString(1), c.getString(2));
//
//                double latitude = c.getDouble(4);
//                double longitude = c.getDouble(5);
//                average_lat += latitude;
//                average_lng += longitude;
//
//                LatLng pos = new LatLng(latitude, longitude);
//                Marker marker = mMap.addMarker(new MarkerOptions()
//                        .position(pos)
//                        .title(full_name));
//                markers.add(marker);
//
//            } else {
//
//                for (int j = 0; j < count1; j++) {
//
//                    c1.moveToPosition(j);
//
//                    if (c1.getString(2).toLowerCase().contains(search_text)) {
//
//                        if (c.getInt(0) == Global.current_user.getId()) is_contain_current_user = true;
//                        String full_name = String.format("%s %s", c.getString(1), c.getString(2));
//
//                        double latitude = c.getDouble(4);
//                        double longitude = c.getDouble(5);
//                        average_lat += latitude;
//                        average_lng += longitude;
//
//                        LatLng pos = new LatLng(latitude, longitude);
//                        Marker marker = mMap.addMarker(new MarkerOptions()
//                                .position(pos)
//                                .title(full_name));
//                        markers.add(marker);
//                        break;
//                    }
//                }
//            }
//        }

//        if (is_contain_current_user) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Global.current_user.getLatitude(), Global.current_user.getLongitude()), 6));
//        } else {
//            average_lat /= count;
//            average_lng /= count;
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(average_lat, average_lng), 6));
//        }
    }

//    @Override
//    public void onInfoWindowClick(Marker marker) {
//        Toast.makeText(this, "Click Info Window", Toast.LENGTH_SHORT).show();
//    }

    private void addKeyword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_keyword, null);
        final EditText edit_title = view.findViewById(R.id.edit_activity_title);
        final EditText edit_description = view.findViewById(R.id.edit_activity_description);
//      final String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");

        builder.setTitle("Add Activity")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String description = edit_description.getText().toString().trim();
                        String title = edit_title.getText().toString().trim();
                        if (title.isEmpty()) {
                            Global.showShortToast(MapsActivity.this, "activity is required.");
                            edit_title.requestFocus();
                            return;
                        }

//                        ContentValues values = new ContentValues();
//                        values.put("uid", Global.current_user.getId());
//                        values.put("keyword", keyword);
//                        Global.mdb.insert("t_keyword", null, values);
                        GPSTracker gpsTracker = new GPSTracker(MapsActivity.this);
                        gpsTracker.getLocation();
//                        CurrentActivity currentActivity = new CurrentActivity(String.valueOf(Global.current_user.getId()), keyword, gpsTracker.latitude, gpsTracker.longitude);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateandTime = sdf.format(new Date());
                        ActivityModel activity = new ActivityModel(String.valueOf(Global.current_user.getId()), title, description, currentDateandTime, gpsTracker.latitude, gpsTracker.longitude);

//                        if (token == null || token == "") {
//                            token = getToken(username);
//                        }

//                        ApiInterface apiService =
//                                HttpClient.getClient().create(ApiInterface.class);
//                        Call<Void> call = apiService.postCurrentLocation("Bearer " + token, currentActivity);
//                        call.enqueue(new Callback<Void>() {
//                            @Override
//                            public void onResponse(Call<Void> call, Response<Void> response) {
//                                if(response.isSuccessful()) {
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<Void> call, Throwable t) {
//                            }
//                        });

                        ApiInterface apiService =
                                HttpClient.getClient().create(ApiInterface.class);
                        Call<Void> call = apiService.postActivity("Bearer " + token, activity);
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
//                        values = new ContentValues();
//                        values.put("latitude", gpsTracker.latitude);
//                        values.put("longitude", gpsTracker.longitude);
//                        Global.mdb.update("t_user", values, "id = " + Global.current_user.getId(), null);

                    }
                })
                .setNegativeButton("Cancel", null);

        builder.show();
    }


    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        private Button infoButton;

        private OnInfoWindowElemTouchListener infoButtonListener;

        private ViewTreeObserver.OnGlobalLayoutListener infoWindowLayoutListener;

        private int popupXOffset;
        private int popupYOffset;

        private class InfoWindowLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
            @Override
            public void onGlobalLayout() {
                //размеры окна изменились, обновляем смещения
                popupXOffset = mWindow.getWidth() / 2;
                popupYOffset = mWindow.getHeight();
            }
        }

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
//            infoWindowLayoutListener = new InfoWindowLayoutListener();
//            mWindow.getViewTreeObserver().addOnGlobalLayoutListener(infoWindowLayoutListener);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
            this.infoButton = (Button)mWindow.findViewById(R.id.button);
            this.infoButtonListener = new OnInfoWindowElemTouchListener(this.infoButton,
                    getResources().getDrawable(R.drawable.round_but_green_sel), //btn_default_normal_holo_light
                    getResources().getDrawable(R.drawable.round_but_red_sel)) //btn_default_pressed_holo_light
            {
                @Override
                protected void onClickConfirmed(View v, Marker marker) {
                    // Here we can perform some action triggered after clicking the button
                    Toast.makeText(MapsActivity.this, marker.getTitle() + "'s button clicked!", Toast.LENGTH_SHORT).show();
                }
            };
            this.infoButton.setOnTouchListener(infoButtonListener);

        }

        @Override
        public View getInfoWindow(Marker marker) {
//        if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_window) {
//            // This means that getInfoContents will be called.
//            return null;
//        }
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
//        if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_contents) {
//            // This means that the default info contents will be used.
//            return null;
//        }
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {
            int badge;
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
//        if (marker.equals(mBrisbane)) {
//            badge = R.drawable.badge_qld;
//        } else if (marker.equals(mAdelaide)) {
//            badge = R.drawable.badge_sa;
//        } else if (marker.equals(mSydney)) {
//            badge = R.drawable.badge_nsw;
//        } else if (marker.equals(mMelbourne)) {
//            badge = R.drawable.badge_victoria;
//        } else if (marker.equals(mPerth)) {
//            badge = R.drawable.badge_wa;
//        } else if (marker.equals(mDarwin1)) {
//            badge = R.drawable.badge_nt;
//        } else if (marker.equals(mDarwin2)) {
//            badge = R.drawable.badge_nt;
//        } else if (marker.equals(mDarwin3)) {
//            badge = R.drawable.badge_nt;
//        } else if (marker.equals(mDarwin4)) {
//            badge = R.drawable.badge_nt;
//        } else {
//            // Passing 0 to setImageResource will clear the image view.
//            badge = 0;
//        }
        ((ImageView) view.findViewById(R.id.badge)).setImageResource(R.drawable.badge_sa);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if (snippet != null && snippet.length() > 12) {
            SpannableString snippetText = new SpannableString(snippet);
//            snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
            snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, snippet.length(), 0);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }
        }
    }
}
