package com.puurva.findmetoo.Activities;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.puurva.findmetoo.Enums.ListViewTypes;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.NotificationRequestModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityNotification;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.ImageUtility;

import java.io.ByteArrayOutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewActivity extends AppCompatActivity implements View.OnClickListener {
        //implements View.OnClickListener {

    private String imageFilePath;
    private String deviceID;
//    private String activityId;
    private ActivityNotification activityNotification;
    ProfileModel profileModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

//        if(Global.preference == null || Global.preference.getValue(this, PrefConst.USERNAME, null) == null){
//            Intent loginIntent = new Intent(this, LoginActivity.class);
//            loginIntent.putExtra("activity", getIntent());
//            loginIntent.putExtras(getIntent().getExtras());
//            startActivity(loginIntent);
//        }

        LoadProfileView();
    }

    private void LoadProfileView() {
        deviceID = getIntent().getStringExtra("DeviceID");
        Button acceptButton = this.findViewById(R.id.btn_profile_accept);
        Button rejectButton = this.findViewById(R.id.btn_profile_reject);
        ImageView profileImage = this.findViewById(R.id.imgViewPhoto);
        acceptButton.setOnClickListener(this);
        rejectButton.setOnClickListener(this);
        RatingBar ratingBar = this.findViewById(R.id.edit_profile_rating);
        ratingBar.setOnTouchListener(new View.OnTouchListener() {
                                         @Override
                                         public boolean onTouch(View v, MotionEvent event) {
                                             if (event.getAction() == MotionEvent.ACTION_UP) {
                                                 launchProfileReviewActivity();
                                             }
                                             return true;
                                         }
                                     });
        //        activityId = getIntent().getStringExtra("ActivityID");
        Log.e("ActivityNotification", "checking activity notification status in Profile View Activity");
        activityNotification = getIntent().getParcelableExtra("ActivityNotification");
        if(activityNotification != null) {
            if (deviceID == null) {
                deviceID = activityNotification.DeviceId;
            }
//        NotificationType notificationType = (NotificationType) getIntent().getSerializableExtra("source");
            if (activityNotification.ActivityNotiicationType == NotificationType.AMIN) {
                acceptButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
            }
        }else{

            acceptButton.setVisibility(View.INVISIBLE);
            rejectButton.setVisibility(View.INVISIBLE);
        }
        if(deviceID != null) {
            fillProfile();
        }
    }

//    @Override
//    public void onResume()
//    {  // After a pause OR at startup
//        super.onResume();
//        LoadProfileView();
//    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_profile_reject:
                handleRequest(RequestStatus.REJECTED);
                finish();
                break;
            case R.id.btn_profile_accept:
                handleRequest(RequestStatus.ACCEPTED);
                finish();
                break;
//            case R.id.profile_rating:
//                launchProfileReviewActivity();
//                break;
            case R.id.imgViewPhoto:
                photoClicked(v);
                break;
        }

    }

    private void launchProfileReviewActivity() {
        Intent profileReviewIntent = new Intent(this, ProfileReviewActivity.class);
        profileReviewIntent.putExtra("ProfileModel", profileModel);
        profileReviewIntent.putExtra("ListSource", ListViewTypes.PROFILEREVIEWS);
        startActivity(profileReviewIntent);
        finish();
    }

    private void handleRequest(RequestStatus requestStatus) {
        final String token = getToken();
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        try {
            NotificationRequestModel notificationRequestModel = new NotificationRequestModel(Global.AndroidID, deviceID, NotificationType.AMIN, activityNotification.ActivityId, requestStatus);
            Call<Void> call = apiService.sendNotification("Bearer " + token, notificationRequestModel);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileViewActivity.this, "Coo, We will let " + deviceID + " know.", Toast.LENGTH_SHORT).show();
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
    }

    private  void photoClicked(View v)
    {
        Drawable drawable = ((ImageView) v).getDrawable();
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
//        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/*");
//        startActivityForResult(photoPickerIntent, 1);
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

    private String getToken() {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (token == null || token == "") {
            token = getToken(username);
        }
        return token;
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

    private void fillProfile()
    {
        final String token = getToken();
        final TextView txtProfileName = ((TextView) findViewById(R.id.txt_view_name));
        final TextView txtHobies = ((TextView) findViewById(R.id.txt_view_hobies));
        final TextView txtAbout = ((TextView) findViewById(R.id.txt_view_about));
        final TextView txtReviews = ((TextView) findViewById(R.id.txt_view_reviews));
        final TextView txtViews = ((TextView) findViewById(R.id.txt_view_views));
        final RatingBar ratingBar = ((RatingBar) findViewById(R.id.edit_profile_rating));
        txtReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent profileReviewsIntent = new Intent(ProfileViewActivity.this, ViewListActivity.class);
                    profileReviewsIntent.putExtra("DeviceId", deviceID);
                    profileReviewsIntent.putExtra("ListSource", ListViewTypes.PROFILEREVIEWS);
                    startActivity(profileReviewsIntent);
                }catch (Exception ex){
                    Log.e("LoadProfileViews", ex.getMessage());
                }
            }
        });
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ProfileModel> call = apiService.getProfile("Bearer " + token, deviceID);
        call.enqueue(new Callback<ProfileModel>() {

            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if(response.isSuccessful()) {
                    profileModel = response.body();
                    if(profileModel !=null) {
                        txtProfileName.setText(profileModel.getProfileName());
                        txtHobies.setText(profileModel.getHobies());
                        txtAbout.setText(profileModel.getAbout());
                        txtReviews.setText(txtReviews.getText() + " : " + ((Long) profileModel.getReviews()).toString());
                        txtViews.setText(txtViews.getText() + " : " + ((Long) profileModel.getViews()).toString());
                        ratingBar.setRating(profileModel.getRating());
                        downloadProfileImage(token, deviceID);

                        profileModel.setViews(profileModel.getViews()+1);
                        profileModel.setDeviceId(deviceID);

                        CommonUtility.PostProfile(token, profileModel, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
            }
        });
    }

    private void downloadProfileImage(String token, String deviceID) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.getProfileImage("Bearer " + token, deviceID);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if(response.isSuccessful()) {
                        Log.d("onResponse", "Response came from server");

                        boolean FileDownloaded = false;
                        if (response.body() != null) {
                            FileDownloaded = DownloadImage(response.body());
                        }
                        Log.d("onResponse", "Image is downloaded and saved ? " + FileDownloaded);
                    }

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
                ImageView image1 = ((ImageView) findViewById(R.id.imgViewPhoto));
                bmp = ImageUtility.scaleImageToResolution(this, bmp, bmp.getHeight(), bmp.getWidth());
                image1.setMaxWidth(bmp.getWidth());
                image1.setMaxHeight(bmp.getHeight());
                image1.setImageBitmap(bmp);
                profileModel.setProfilePhoto(bmp);
            }
            return true;

        } catch (Exception e) {
            Log.d("DownloadImage", e.toString());
            return false;
        }
    }

}
