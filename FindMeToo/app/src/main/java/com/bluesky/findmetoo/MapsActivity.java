package com.bluesky.findmetoo;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.bluesky.findmetoo.model.CurrentActivity;
import com.bluesky.findmetoo.uitls.GPSTracker;
import com.bluesky.findmetoo.uitls.Global;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.bluesky.findmetoo.ServiceInterfaces.*;
import com.bluesky.findmetoo.uitls.*;
import retrofit2.Call;
import retrofit2.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, FloatingSearchView.OnMenuItemClickListener, FloatingSearchView.OnSearchListener, FloatingSearchView.OnHomeActionClickListener {

    private GoogleMap mMap;
    private List<Marker> markers;
    private FloatingSearchView mSearchView;

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

    private void showMarkerOfUsers(String search_text) {

        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<List<CurrentActivity>> call = apiService.getMatchingActivities(search_text);
        call.enqueue(new Callback<List<CurrentActivity>>() {
            @Override
            public void onResponse(Call<List<CurrentActivity>> call, Response<List<CurrentActivity>> response) {
                List<CurrentActivity> locations = response.body();
            }

            @Override
            public void onFailure(Call<List<CurrentActivity>> call, Throwable t) {
            }
        });
    //return;

        // all markers remove
        if (markers != null) {
            for (Marker marker : markers) marker.remove();
        }

        search_text = search_text.toLowerCase();

        // search all user from db as search text
        Cursor c = Global.mdb.rawQuery("SELECT * FROM t_user",null);

        int count = c.getCount();
        markers = new ArrayList<>();

        boolean is_contain_current_user = false;
        double average_lat = 0, average_lng = 0;

        for (int i = 0; i < count; i++) {

            c.moveToPosition(i);
            Cursor c1 = Global.mdb.rawQuery("SELECT * FROM t_keyword WHERE uid = " + c.getInt(0), null);

            int count1 = c1.getCount();

            if (c.getString(1).toLowerCase().contains(search_text) ||
                    c.getString(2).toLowerCase().contains(search_text) ||
                    search_text.isEmpty()) {

                if (c.getInt(0) == Global.current_user.getId()) is_contain_current_user = true;
                String full_name = String.format("%s %s", c.getString(1), c.getString(2));

                double latitude = c.getDouble(4);
                double longitude = c.getDouble(5);
                average_lat += latitude;
                average_lng += longitude;

                LatLng pos = new LatLng(latitude, longitude);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(full_name));
                markers.add(marker);

            } else {

                for (int j = 0; j < count1; j++) {

                    c1.moveToPosition(j);

                    if (c1.getString(2).toLowerCase().contains(search_text)) {

                        if (c.getInt(0) == Global.current_user.getId()) is_contain_current_user = true;
                        String full_name = String.format("%s %s", c.getString(1), c.getString(2));

                        double latitude = c.getDouble(4);
                        double longitude = c.getDouble(5);
                        average_lat += latitude;
                        average_lng += longitude;

                        LatLng pos = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(full_name));
                        markers.add(marker);
                        break;
                    }
                }
            }
        }

        if (is_contain_current_user) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Global.current_user.getLatitude(), Global.current_user.getLongitude()), 6));
        } else {
            average_lat /= count;
            average_lng /= count;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(average_lat, average_lng), 6));
        }
    }

    private void addKeyword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_keyword, null);
        final EditText edit_keyword = view.findViewById(R.id.edit_keyword);

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

                        ContentValues values = new ContentValues();
                        values.put("uid", Global.current_user.getId());
                        values.put("keyword", keyword);
                        Global.mdb.insert("t_keyword", null, values);

                        GPSTracker gpsTracker = new GPSTracker(MapsActivity.this);
                        gpsTracker.getLocation();

                        values = new ContentValues();
                        values.put("latitude", gpsTracker.latitude);
                        values.put("longitude", gpsTracker.longitude);
                        Global.mdb.update("t_user", values, "id = " + Global.current_user.getId(), null);

                    }
                })
                .setNegativeButton("Cancel", null);

        builder.show();
    }

}
