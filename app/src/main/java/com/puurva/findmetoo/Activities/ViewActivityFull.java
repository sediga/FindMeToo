package com.puurva.findmetoo.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.puurva.findmetoo.Enums.ActivityTypes;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.CurrentActivity;
import com.puurva.findmetoo.ServiceInterfaces.model.NotificationRequestModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.ImageUtility;
import com.puurva.findmetoo.uitls.ProfileListAdapter;
import com.puurva.findmetoo.uitls.SQLHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewActivityFull extends AppCompatActivity implements View.OnClickListener {

    LinearLayout mainLayout;
    private Button imIn;
    private ImageButton infoImage;
    private Button viewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_info_window);

        mainLayout = this.findViewById(R.id.info_window_main);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        getLayoutInflater().inflate(R.layout.custom_info_window, null);
        mainLayout.setLayoutParams(params);

        final CurrentActivity activity = getIntent().getParcelableExtra("Activity");
        if(activity != null) {
            render(activity);
        }

        this.imIn = findViewById(R.id.iammin);
        this.infoImage = findViewById(R.id.info_badge);
        this.viewProfile = findViewById(R.id.view_profile_link);

        this.imIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                Toast.makeText(ViewActivityFull.this, "Your request has been submitted. We will let you know when accepted.", Toast.LENGTH_SHORT).show();
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
        });

        this.infoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchImageViewer(view);
            }
        });

        this.viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchViewProfile(activity.DeviceId, false);
            }
        });
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

    private void LaunchViewProfile(String deviceID, boolean isFromNotification) {
        Intent profileIntent = new Intent(this, ProfileViewActivity.class);
        if (deviceID != null) {
            profileIntent.putExtra("DeviceID", deviceID);
        }
//        if (activityNotification != null && isFromNotification) {
//            profileIntent.putExtra("ActivityNotification", activityNotification);
//        }
        startActivity(profileIntent);

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void render(CurrentActivity activity){
        try {
            String title = activity.Activity;
            TextView titleUi = this.findViewById(R.id.title);
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = activity.Description;
            TextView snippetUi = this.findViewById(R.id.snippet);
            snippetUi.setSelected(true);
            if (snippet != null) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }

//            snippetUi.setMaxHeight(getWindowManager().getDefaultDisplay().getHeight());
            snippetUi.setMaxHeight(20 * snippetUi.getLineHeight());
//            snippetUi.setHeight(20 * snippetUi.getLineHeight());
//            snippetUi.setHeight(500);
            mainLayout.invalidate();
//            snippetUi.getLayoutParams().height = 200;
            if (activity != null) {
                ((RatingBar) findViewById(R.id.profile_rating_indicator)).setRating(activity.ProfileRating);
                if(activity.ActivityStartTime != null) {
                    TextView startTime = findViewById(R.id.start_time);
                    Date startDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(activity.ActivityStartTime);
                    String formattedFromDate = Global.activityDateFormat.format(startDateTime);
                    startTime.setText(formattedFromDate);
                }
                if(activity.ActivityStartTime != null) {
                    TextView endTime = findViewById(R.id.end_time);
                    Date endDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(activity.ActivityEndTime);
                    String formattedToDate = Global.activityDateFormat.format(endDateTime);
                    endTime.setText(formattedToDate);
                }
            }


            String token = getToken();
//        final Marker tempMarker = marker;
            ImageButton imageButton = (ImageButton) this.findViewById(R.id.info_badge);
            if (activity.ImagePath != "" && !ImageUtility.SetImage(CommonUtility.GetFilePath() + activity.ImagePath.toString().split("\\\\")[1] + ".png",
                    imageButton, 200, 250)) {
                ImageUtility.GetActivityImage(activity.ImagePath, imageButton, token, 200, 250);
            } else if (activity.ImagePath == "" || activity.ImagePath == null) {
                ((ImageButton) this.findViewById(R.id.info_badge)).setImageResource(0);
            }
        }catch (Exception ex){
            Log.e("RenderActivityFullView", ex.getMessage(), ex);
        }
        //        else if (marker.getTag() == "") {
//            ImageButton image1 = ((ImageButton) mWindow.findViewById(R.id.info_badge));
//            image1.setImageResource(0);
//        }
    }

    private String getToken() {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (token == null || token == "") {
            token = SQLHelper.getToken(username);
        }
        return token;
    }

}
