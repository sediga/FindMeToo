package com.bluesky.findmetoo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.bluesky.findmetoo.ServiceInterfaces.*;
import com.bluesky.findmetoo.uitls.*;

import okhttp3.ResponseBody;
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
    private View mWindow;

//        private final View mContents;

    private Button imIn;
    private ImageButton infoImage;

    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoImageButtonListener;

    private ViewTreeObserver.OnGlobalLayoutListener infoWindowLayoutListener;

    private MapWrapperLayout mapWrapperLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);

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
        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 20));

        this.imIn = (Button) mWindow.findViewById(R.id.iammin);
        this.infoImage = ((ImageButton) mWindow.findViewById(R.id.info_badge));
        this.infoButtonListener = new OnInfoWindowElemTouchListener(this.imIn,
                getResources().getDrawable(R.drawable.btn_bg), //btn_default_normal_holo_light
                getResources().getDrawable(R.drawable.btn_bg)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                Toast.makeText(MapsActivity.this, marker.getTitle() + "'s button clicked!", Toast.LENGTH_SHORT).show();
            }
        };
        this.imIn.setOnTouchListener(infoButtonListener);

        this.infoImageButtonListener = new OnInfoWindowElemTouchListener(this.infoImage,
                getResources().getDrawable(R.drawable.badge_sa), //btn_default_normal_holo_light
                getResources().getDrawable(R.drawable.badge_sa)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
//                Toast.makeText(MapsActivity.this, marker.getTitle() + "'s image button clicked!", Toast.LENGTH_SHORT).show();
            }
        };
        this.infoImage.setOnTouchListener(infoImageButtonListener);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
//            Toast.makeText(MapsActivity.this, "getInfoContents Called", Toast.LENGTH_SHORT).show();
//                mWindow.setTag();
                ImageButton image = ((ImageButton) mWindow.findViewById(R.id.info_badge));
                render(marker, mWindow);
                infoButtonListener.setMarker(marker);
                infoImageButtonListener.setMarker(marker);
                mapWrapperLayout.setMarkerWithInfoWindow(marker, mWindow);
                if(image.getTag(R.id.info_badge) == "loaded") {
//                    if(image.getTag(1) == "yes") {
//                        marker.showInfoWindow();
//                        image.setTag(1, "no");
//                    }
                    marker.showInfoWindow();
                    image.setTag(R.id.info_badge, "DontReload");
                }
                return mWindow;
            }

        });
    }

    private void render(Marker marker, View view) {
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
//        Toast.makeText(MapsActivity.this, "Snippet : ", Toast.LENGTH_SHORT).show();
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
//            Toast.makeText(MapsActivity.this, "Snippet : " + snippetText, Toast.LENGTH_SHORT).show();
//            snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
            snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, snippet.length(), 0);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }

        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (token == null || token == "") {
            token = getToken(username);
        }

        ImageButton image = ((ImageButton) mWindow.findViewById(R.id.info_badge));
        if(image.getTag(R.id.info_badge) != "loaded") {
            ApiInterface apiService =
                    HttpClient.getClient().create(ApiInterface.class);
            Call<ResponseBody> call = apiService.getMatchingImages("Bearer " + token, title);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {

                        Log.d("onResponse", "Response came from server");

                        boolean FileDownloaded = false;
                        if (response.body() != null) {
                            FileDownloaded = DownloadImage(response.body());
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
//        ((ImageButton) view.findViewById(R.id.info_badge)).setImageResource(R.drawable.badge_sa);
    }

    private boolean DownloadImage(ResponseBody body) {

        try {
            Log.d("DownloadImage", "Reading and writing file");
            InputStream in = null;
            FileOutputStream out = null;

            try {
                in = body.byteStream();
                out = new FileOutputStream(getExternalFilesDir(null) + File.separator + "AndroidTutorialPoint.jpg");
                int c;

                while ((c = in.read()) != -1) {
                    out.write(c);
                }
            }
            catch (IOException e) {
                Log.d("DownloadImage",e.toString());
                return false;
            }
            finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }

            int width, height;
            ImageButton image = ((ImageButton) mWindow.findViewById(R.id.info_badge));
            if(image.getTag(R.id.info_badge) != "loaded") {
                Bitmap bMap = BitmapFactory.decodeFile(getExternalFilesDir(null) + File.separator + "AndroidTutorialPoint.jpg");
                width = 4 * bMap.getWidth();
                height = 4 * bMap.getHeight();
                Bitmap bMap2 = Bitmap.createScaledBitmap(bMap, width, height, false);
                image.setImageBitmap(bMap2);
//            image.invalidate();
//                if(image.getTag(R.id.imgPhoto) == null) {
//                    image.setTag(R.id.info_badge, "loaded");
//                }
//                image.setTag(1, "yes");
            }
            return true;

        } catch (IOException e) {
            Log.d("DownloadImage",e.toString());
            return false;
        }
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
        } else {
            markers = new HashMap<Integer, Marker>();
        }

        if (circles != null) {
            circles.clear();
//            for (Circle circle : circles) circle.remove();
        } else {
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
                if (response.isSuccessful()) {
                    final Object[] locations = (response.body().toArray());
                    for (int i = 0; i < locations.length; i++) {
                        final String imagePath = ((CurrentActivity) locations[i]).ImagePath;
                        LatLng pos = new LatLng(((CurrentActivity) locations[i]).latitude, ((CurrentActivity) locations[i]).longitude);
                        final Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(pos).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("custom_info_bubble", 1, 1)))
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
//                        Log.e("Desctiption", ((CurrentActivity) locations[i]).description);
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
//                                marker.setVisible(false);
                                //infoWindow.setVisibility(View.VISIBLE);
                                marker.setTag(imagePath);
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

            public Bitmap resizeMapIcons(String iconName, int width, int height) {
                Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
                return resizedBitmap;
            }

            @Override
            public void onFailure(Call<List<CurrentActivity>> call, Throwable t) {
            }
        });

    }

    private void addKeyword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_keyword, null);
        final EditText edit_title = view.findViewById(R.id.edit_activity_title);
        final EditText edit_description = view.findViewById(R.id.edit_activity_description);
        final ImageView image = view.findViewById(R.id.edit_activity_badge);
//      final String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");

        OnInfoWindowElemTouchListener imageListener;

        image.setClickable(true);
        imageListener = new OnInfoWindowElemTouchListener(image,
                getResources().getDrawable(R.drawable.badge_sa), //btn_default_normal_holo_light
                getResources().getDrawable(R.drawable.badge_sa)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
//                Toast.makeText(MapsActivity.this, "image button clicked!", Toast.LENGTH_SHORT).show();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        };
        image.setOnTouchListener(imageListener);


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
                        Log.e("PostActivity", "Post Activity called");
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if(response.isSuccessful()) {
                                    Log.e("PostActivity", "success returned");
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("PostActivity", "Error on postActivity" + t.getMessage());
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

    private String imageFilePath;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Test", "OnActivityResult Called");
        Toast.makeText(MapsActivity.this, "onActivityResult called", Toast.LENGTH_SHORT);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();

                this.imageFilePath = getPath(selectedImage);
                Log.e("FilePath", this.imageFilePath);
                String file_extn = this.imageFilePath.substring(this.imageFilePath.lastIndexOf(".") + 1);
//                image_name_tv.setText(imageFilePath);

//                try {
                if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                    Toast.makeText(MapsActivity.this,"filename: " + this.imageFilePath, Toast.LENGTH_SHORT);
                    //FINE
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(this.imageFilePath, options);
                    ((ImageView) findViewById(R.id.imgPhoto)).setImageBitmap(bitmap);
                } else {
                    Log.e("ImageError", "Unknown photo file format");
                    //NOT IN REQUIRED FORMAT
                }
//                } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

}
