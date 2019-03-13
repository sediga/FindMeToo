package com.puurva.findmetoo.uitls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.puurva.findmetoo.LoginActivity;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.DeviceModel;
import com.puurva.findmetoo.model.ProfileModel;
import com.puurva.findmetoo.model.ProfileReviewModel;
import com.puurva.findmetoo.preference.PrefConst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommonUtility {

    private static final String infoWindowImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Global.FILE_PATH_SUFFIX;

    public static void RegisterDevice(DeviceModel deviceModel) {
        final ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
//        TokenBindingModel tokenBindingModel = new TokenBindingModel(username, "password", password);
        Call<Void> tokenCall = apiService.postDevice(deviceModel);
        tokenCall.enqueue((new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Global.has_device_registered = true;
//                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println(t.getMessage());
                Log.e("login", "Login Failed : " + t.getMessage());
            }
        }));
    }

    public  static String GetDeviceId()
    {
        String deviceId = Global.AndroidID;
        if(deviceId == null)
        {
            DeviceModel latestStoredDevice = SQLHelper.GetLatestDevice();
            if(latestStoredDevice != null) {
                deviceId = latestStoredDevice.DeviceID;
            }
        }
        return deviceId;
    }

    public static void PostProfile(final String token, final ProfileModel profileModel, final Bitmap bitmap) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.putProfile("Bearer " + token, profileModel.getDeviceId(), profileModel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    if(bitmap != null) {
                        uploadImage(profileModel.getDeviceId(), token, bitmap);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    public static void PostProfileReview(final String token, final ProfileReviewModel profileReviewModel) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.postProfileReview("Bearer " + token, profileReviewModel);
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
    }

    public static void uploadImage(String deviceId, String token, Bitmap bitmap) {
        String tempFileName = GetImageFileFullPath();
        if (bitmap != null) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(tempFileName);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                File file = new File(tempFileName);
//            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), requestBody);

                RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");
                ApiInterface apiService =
                        HttpClient.getClient().create(ApiInterface.class);
                Call<Void> call = apiService.postProfileImage("Bearer " + token, deviceId, body);
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public static String GetImageFileFullPath() {
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
}


