package com.bluesky.findmetoo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.bluesky.findmetoo.ServiceInterfaces.*;
import com.bluesky.findmetoo.uitls.*;

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }
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
        final MapWrapperLayout mapWrapperLayout = findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 20));

        this.imIn = mWindow.findViewById(R.id.iammin);
        this.infoImage = mWindow.findViewById(R.id.info_badge);
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

        this.infoImageButtonListener = new OnInfoWindowElemTouchListener(this.infoImage, null, null)
//                getResources().getDrawable(R.drawable.badge_sa), //btn_default_normal_holo_light
//                getResources().getDrawable(R.drawable.badge_sa)) //btn_default_pressed_holo_light
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
                ImageButton image = mWindow.findViewById(R.id.info_badge);
                try {
                    render(marker, mWindow);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                infoButtonListener.setMarker(marker);
                infoImageButtonListener.setMarker(marker);
                mapWrapperLayout.setMarkerWithInfoWindow(marker, mWindow);
                if (image.getTag(R.id.info_badge) == "loaded") {
                    image.setTag(R.id.info_badge, "DontReload");
                }
                return mWindow;
            }
        });
    }

    private void render(final Marker marker, View view) throws IOException {
        String title = marker.getTitle();
        TextView titleUi = view.findViewById(R.id.title);
        if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = view.findViewById(R.id.snippet);
//        Toast.makeText(MapsActivity.this, "Snippet : ", Toast.LENGTH_SHORT).show();
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
//            Toast.makeText(MapsActivity.this, "Snippet : " + snippetText, Toast.LENGTH_SHORT).show();
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
//        final Marker tempMarker = marker;
        if(marker.getTag() != "downloaded" && marker.getTag() != "") {
            String deviceId = marker.getTag().toString().split("/")[0];
            String fileName = marker.getTag().toString().split("/")[1];
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
    }

    private boolean DownloadImage(ResponseBody body) {

        try {
            Log.d("DownloadImage", "Reading and writing file");
            if (body != null) {
                // display the image data in a ImageView or save it
                Bitmap bmp = BitmapFactory.decodeStream(body.byteStream());

                int width, height;
                ImageButton image1 = ((ImageButton) mWindow.findViewById(R.id.info_badge));
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

    @Override
    public void onActionMenuItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_search) {
            showMarkerOfUsers(mSearchView.getQuery());
        } else if (id == R.id.action_add) {
            addKeyword();
        } else if (id == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }

    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
    }

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
        } else {
            markers = new HashMap<Integer, Marker>();
        }

        if (circles != null) {
            circles.clear();
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
                        marker.setTag(((CurrentActivity) locations[i]).ImagePath);
                        Circle circle = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(((CurrentActivity) locations[i]).latitude, ((CurrentActivity) locations[i]).longitude))
                                .radius(500)
                                .clickable(true)
                                .strokeColor(Color.MAGENTA)
                                .fillColor(0x220000FF));
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

    View view = null;
    ImageView image = null;

    private void addKeyword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        view = LayoutInflater.from(this).inflate(R.layout.dialog_keyword, null);
        final EditText edit_title = view.findViewById(R.id.edit_activity_title);
        final EditText edit_description = view.findViewById(R.id.edit_activity_description);
        image = view.findViewById(R.id.edit_activity_badge);
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");

        image.setImageResource(R.drawable.takeaphoto);
        OnInfoWindowElemTouchListener imageListener;
        Button btnPick = view.findViewById(R.id.button_pick);
        btnPick.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                }catch (Exception ex)
                {
                    Log.e("cameraintenterror", ex.getMessage());
                }
            }
        }
        );
        image.setClickable(true);
        imageListener = new OnInfoWindowElemTouchListener(image, null, null)
//                getResources().getDrawable(R.drawable.badge_sa), //btn_default_normal_holo_light
//                getResources().getDrawable(R.drawable.badge_sa)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
//                Toast.makeText(MapsActivity.this, "image button clicked!", Toast.LENGTH_SHORT).show();
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/tempImage.jpeg";
                startActivityForResult(takePicture, 0);
            }
        };
        image.setOnTouchListener(imageListener);

        builder.setTitle("Add Activity")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String deviceId = String.valueOf(Global.current_user.getId());
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
                        final ActivityModel activity = new ActivityModel(String.valueOf(Global.current_user.getId()), title, description, currentDateandTime, gpsTracker.latitude, gpsTracker.longitude);

                        ApiInterface apiService =
                                HttpClient.getClient().create(ApiInterface.class);
                        Call<Void> call = apiService.postActivity("Bearer " + token, activity);
                        Log.e("PostActivity", "Post Activity called");
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
                })
                .setNegativeButton("Cancel", null);
        builder.show();
//        AlertDialog dialog = builder.create();
//        dialog.getWindow().setLayout(900, 900); //Controlling width and height.
//        dialog.show();
    }

    private String imageFilePath;
    private String imageFileName;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Test", "OnActivityResult Called");
        Toast.makeText(MapsActivity.this, "onActivityResult called", Toast.LENGTH_SHORT);
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage = null;
        try {
            Bitmap bitmap = null;
            this.imageFilePath = null;
            this.imageFileName = null;
            switch (requestCode) {
                case 1: {
                    if (resultCode == Activity.RESULT_OK) {
                        selectedImage = data.getData();
                        this.imageFilePath = getPath(selectedImage);
                        Log.e("FilePath", this.imageFilePath);
                        String file_extn = this.imageFilePath.substring(this.imageFilePath.lastIndexOf(".") + 1);
                        this.imageFileName = "tempImage." + file_extn;

                        if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                            Toast.makeText(MapsActivity.this, "filename: " + this.imageFilePath, Toast.LENGTH_SHORT);
                            //FINE
                        } else {
                            Log.e("ImageError", "Unknown photo file format");
                            //NOT IN REQUIRED FORMAT
                        }                    }
                }
                break;
                case 0: {
                    if (resultCode == RESULT_OK) {
                            if (data != null && data.getExtras() != null) {
                                this.imageFileName = "/tempImage.jpeg";
                                this.imageFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+this.imageFileName;
                                File file = new File(this.imageFilePath);
                                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                                FileOutputStream out = new FileOutputStream(file);
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                out.flush();
                                out.close();
//                                imageBitmap.s
//                                mImageView.setImageBitmap(imageBitmap);
                        }
//                        selectedImage = data.getData();
//                        this.imageFilePath = selectedImage.getPath();
                    }
                }
                break;
            }

            if (image != null && this.imageFileName != null && this.imageFilePath != null) {
                this.imageFilePath = ImageUtility.resizeAndCompressImageBeforeSend(this, this.imageFilePath, this.imageFileName);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFile(imageFilePath, options);
                image.setImageBitmap(bitmap);
            }
        }catch (Exception ex){
            Log.e("imageselector", ex.getMessage());
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

    private void uploadImage(String deviceId, String activity, String token) {
        try {
            File file = new File(this.imageFilePath);
//            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

            MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), requestBody);

            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

            ApiInterface apiService =
                    HttpClient.getClient().create(ApiInterface.class);
            Call<Void> call = apiService.postActivityImage("Bearer " + token, deviceId, activity, body);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        } catch (Exception ex) {
            Log.e("ActivityImageUpload", ex.getMessage());
        }
    }
}
