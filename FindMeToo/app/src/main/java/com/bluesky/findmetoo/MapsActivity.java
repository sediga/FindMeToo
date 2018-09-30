package com.bluesky.findmetoo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.List;

import com.bluesky.findmetoo.ServiceInterfaces.*;
import com.bluesky.findmetoo.uitls.*;
import retrofit2.Call;
import retrofit2.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, FloatingSearchView.OnMenuItemClickListener, FloatingSearchView.OnSearchListener, FloatingSearchView.OnHomeActionClickListener {

    private GoogleMap mMap;
    private List<Marker> markers;
    private List<Circle> circles;
    private FloatingSearchView mSearchView;
    private ViewGroup infoWindow;

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
    }


    @Override
    public void onActionMenuItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_search) {
            showMarkerOfUsers(mSearchView.getQuery());
        } else if (id == R.id.action_add) {
            addKeyword();
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
        } else {
            c.moveToFirst();
            token = c.getString(2);
        }
        return token;
    }

    private void showMarkerOfUsers(String search_text) {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");

        // all markers remove
        if (markers != null) {
            for (Marker marker : markers) marker.remove();
        }
        else {
            markers = new ArrayList<>();
        }

        if (circles != null) {
            for (Circle circle : circles) circle.remove();
        }
        else {
            circles = new ArrayList<>();
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
//                        Marker marker = mMap.addMarker(new MarkerOptions()
//                                .position(pos)
//                                .title(((CurrentActivity) locations[i]).activity));
//                        markers.add(marker);
                        Circle circle = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(((CurrentActivity) locations[i]).latitude, ((CurrentActivity) locations[i]).longitude))
                                .radius(500)
                                .clickable(true)
                                .strokeColor(Color.MAGENTA)
                                .fillColor(0x220000FF));

                        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

                            @Override
                            public void onCircleClick(Circle circle) {
                                //infoWindow.setVisibility(View.VISIBLE);
                                circle.setTag("test");
                                // Flip the r, g and b components of the circle's
                                // stroke color.
//                                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
//                                circle.setStrokeColor(strokeColor);

                             }
                        });
                            circles.add(circle);
                    }
                }
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

    private void addKeyword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_keyword, null);
        final EditText edit_keyword = view.findViewById(R.id.edit_keyword);
//        final String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");

        builder.setTitle("Add Keyword")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String keyword = edit_keyword.getText().toString().trim();
                        if (keyword.isEmpty()) {
                            Global.showShortToast(MapsActivity.this, "Keyword is required.");
                            edit_keyword.requestFocus();
                            return;
                        }

//                        ContentValues values = new ContentValues();
//                        values.put("uid", Global.current_user.getId());
//                        values.put("keyword", keyword);
//                        Global.mdb.insert("t_keyword", null, values);
                        GPSTracker gpsTracker = new GPSTracker(MapsActivity.this);
                        gpsTracker.getLocation();
                        CurrentActivity currentActivity = new CurrentActivity(String.valueOf(Global.current_user.getId()), keyword, gpsTracker.latitude, gpsTracker.longitude);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateandTime = sdf.format(new Date());
                        ActivityModel activity = new ActivityModel(String.valueOf(Global.current_user.getId()), keyword,currentDateandTime );

//                        if (token == null || token == "") {
//                            token = getToken(username);
//                        }

                        ApiInterface apiService =
                                HttpClient.getClient().create(ApiInterface.class);
                        Call<Void> call = apiService.postCurrentLocation("Bearer " + token, currentActivity);
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

                        apiService =
                                HttpClient.getClient().create(ApiInterface.class);
                        call = apiService.postActivity("Bearer " + token, activity);
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

}
