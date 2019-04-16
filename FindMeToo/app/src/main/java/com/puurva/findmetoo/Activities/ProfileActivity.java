package com.puurva.findmetoo.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.puurva.findmetoo.Enums.ListViewTypes;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.ImageUtility;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int CAMERA_ACTIVITY = 1;
    public static final int GALLERY_ACTIVITY = 0;
    private String imageFilePath;
    private String deviceID;
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.imgPhoto).setOnClickListener(this);
        deviceID = getIntent().getStringExtra("DeviceID");
        if(deviceID != null) {
            fillProfile();
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_save:
                addProfile();
                break;
            case R.id.imgPhoto:
                photoClicked();
                break;
        }

    }

    private  void photoClicked()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_ACTIVITY);
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

            ImageView image = ((ImageView) findViewById(R.id.imgPhoto));

            if (image != null && bitmap != null) {
//                bitmap = ImageUtility.scaleImageToResolution(this, this.bitmap, image.getWidth(), image.getWidth());
                image.setImageBitmap(bitmap);
            }

        }catch (Exception ex){
            Log.e("imageselector", ex.getMessage());
        }
    }

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.e("Test", "OnActivityResult Called");
////        Toast.makeText(ProfileActivity.this, "onActivityResult called", Toast.LENGTH_SHORT);
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1)
//            if (resultCode == Activity.RESULT_OK) {
//                Uri selectedImage = data.getData();
//
//                this.imageFilePath = getPath(selectedImage);
//                Log.e("FilePath", this.imageFilePath);
//                String file_extn = this.imageFilePath.substring(this.imageFilePath.lastIndexOf(".") + 1);
////                image_name_tv.setText(imageFilePath);
//
////                try {
//                if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
//                    Toast.makeText(ProfileActivity.this,"filename: " + this.imageFilePath, Toast.LENGTH_SHORT);
//                    //FINE
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                    Bitmap bitmap = BitmapFactory.decodeFile(this.imageFilePath, options);
//                    ((ImageView) findViewById(R.id.imgPhoto)).setImageBitmap(bitmap);
//                } else {
//                    Log.e("ImageError", "Unknown photo file format");
//                    //NOT IN REQUIRED FORMAT
//                }
////                } catch (FileNotFoundException e) {
////                    // TODO Auto-generated catch block
////                    e.printStackTrace();
////                }
//            }
//    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

    private void fillProfile()
    {
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        final EditText txtProfileName = ((EditText) findViewById(R.id.txt_name));
        final EditText txtHobies = ((EditText) findViewById(R.id.txt_hobies));
        final EditText txtAbout = ((EditText) findViewById(R.id.txt_about));
        final TextView txtReviews = ((TextView) findViewById(R.id.txt_view_reviews));
        final TextView txtViews = ((TextView) findViewById(R.id.txt_view_views));
        final RatingBar ratingBar = ((RatingBar) findViewById(R.id.profile_rating));
        txtReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent profileReviewsIntent = new Intent(ProfileActivity.this, ViewListActivity.class);
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
                    ProfileModel profileModel = response.body();
                    if(profileModel!=null) {
                        txtProfileName.setText(profileModel.getProfileName());
                        txtHobies.setText(profileModel.getHobies());
                        txtAbout.setText(profileModel.getAbout());
                        txtReviews.setText(txtReviews.getText() + " : " + ((Long) profileModel.getReviews()).toString());
                        txtViews.setText(txtViews.getText() + " : " + ((Long) profileModel.getViews()).toString());
                        ratingBar.setRating(profileModel.getRating());
                        downloadProfileImage(token, deviceID);
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

    private boolean DownloadImage(ResponseBody body) {

        try {
            Log.d("DownloadImage", "Reading and writing file");
            if (body != null) {
                // display the image data in a ImageView or save it
                Bitmap bmp = BitmapFactory.decodeStream(body.byteStream());

                int width, height;
                ImageView image1 = ((ImageView) findViewById(R.id.imgPhoto));
                bmp = ImageUtility.scaleImageToResolution(bmp, bmp.getHeight(), bmp.getWidth());
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

    private void addProfile() {
        String profilName = ((EditText) findViewById(R.id.txt_name)).getText().toString();
        String hobies = ((EditText) findViewById(R.id.txt_hobies)).getText().toString();
        String about = ((EditText) findViewById(R.id.txt_about)).getText().toString();
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");

        ProfileModel profileModel = new ProfileModel(deviceID, Global.preference.getValue(this,
                PrefConst.USERNAME, ""),
                null, profilName, hobies, about);

        CommonUtility.PostProfile(token, profileModel, this.bitmap);
        finish();
    }

}
