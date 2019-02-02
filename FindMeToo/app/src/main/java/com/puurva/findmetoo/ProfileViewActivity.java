package com.puurva.findmetoo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.model.ProfileModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;

import org.w3c.dom.Text;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewActivity extends AppCompatActivity {
        //implements View.OnClickListener {

    private String imageFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

//        findViewById(R.id.btn_save).setOnClickListener(this);
//        findViewById(R.id.btn_cancel).setOnClickListener(this);
//        findViewById(R.id.imgPhoto).setOnClickListener(this);

        fillProfile();
    }

//    @Override
//    public void onClick(View v) {
//
//        int id = v.getId();
//
//        switch (id) {
//            case R.id.btn_cancel:
//                finish();
//                break;
//            case R.id.btn_save:
//                addProfile();
//                break;
//            case R.id.imgPhoto:
//                photoClicked();
//                break;
//        }
//
//    }

    private  void photoClicked()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

//    @Override
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
//                    Toast.makeText(ProfileViewActivity.this,"filename: " + this.imageFilePath, Toast.LENGTH_SHORT);
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
////
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
        String deviceId = String.valueOf(Global.current_user.getId());
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        final TextView txtProfileName = ((TextView) findViewById(R.id.txt_view_name));
        final TextView txtHobies = ((TextView) findViewById(R.id.txt_view_hobies));
        final TextView txtAbout = ((TextView) findViewById(R.id.txt_view_about));
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ProfileModel> call = apiService.getProfile("Bearer " + token, deviceId);
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if(response.isSuccessful()) {
                    ProfileModel profileModel = response.body();
                    if(profileModel!=null) {
                        txtProfileName.setText(profileModel.getProfileName());
                        txtHobies.setText(profileModel.getHobies());
                        txtAbout.setText(profileModel.getAbout());
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
            }
        });

    }

//    private void addProfile() {
//        final String deviceId = String.valueOf(Global.current_user.getId());
//        String profilName = ((EditText) findViewById(R.id.txt_name)).getText().toString();
//        String hobies = ((EditText) findViewById(R.id.txt_hobies)).getText().toString();
//        String about = ((EditText) findViewById(R.id.txt_about)).getText().toString();
//        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
//
//        ProfileModel profileModel = new ProfileModel(String.valueOf(Global.current_user.getId()), Global.preference.getValue(this,
//                PrefConst.USERNAME, ""),
//                null, profilName, hobies, about);
//
//        ApiInterface apiService =
//                HttpClient.getClient().create(ApiInterface.class);
//        Call<Void> call = apiService.putProfile("Bearer " + token, deviceId, profileModel);
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if(response.isSuccessful()) {
////                    uploadImage(deviceId, token);
//                    finish();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//            }
//        });
//    }
//    private void uploadImage(String deviceId, String token) {
//        File file = new File(this.imageFilePath);
//        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
//        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");
//        ApiInterface apiService =
//                HttpClient.getClient().create(ApiInterface.class);
//        Call<Void> call = apiService.postImage("Bearer " + token, deviceId, body);
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if(response.isSuccessful()) {
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//            }
//        });
//    }
}
