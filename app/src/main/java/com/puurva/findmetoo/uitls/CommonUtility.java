package com.puurva.findmetoo.uitls;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileReviewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommonUtility {

    private static final String infoWindowImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Global.FILE_PATH_SUFFIX;

    public static void RegisterDevice(final DeviceModel deviceModel) {
        try {
            final ApiInterface apiService =
                    HttpClient.getClient().create(ApiInterface.class);
//        TokenBindingModel tokenBindingModel = new TokenBindingModel(username, "password", password);
            Call<Void> tokenCall = apiService.postDevice(deviceModel);
            tokenCall.enqueue((new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        if(SQLHelper.UpsertDevice(deviceModel)) {
                            Global.has_device_registered = true;
                        }
//                    finish();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Global.has_device_registered = false;
                    System.out.println(t.getMessage());
                    Log.e("login", "Login Failed : " + t.getMessage());
                }
            }));
        }catch (Exception ex){
            Log.e("RegisterDevice", ex.getMessage());
        }
    }

    public static void RegisterDevice(final DeviceModel deviceModel, final CallBackHelper callBackHelper) {
        try {
            final ApiInterface apiService =
                    HttpClient.getClient().create(ApiInterface.class);
//        TokenBindingModel tokenBindingModel = new TokenBindingModel(username, "password", password);
            Call<Void> tokenCall = apiService.postDevice(deviceModel);
            tokenCall.enqueue((new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        if(SQLHelper.UpsertDevice(deviceModel)) {
                            Global.has_device_registered = true;
                            if(callBackHelper != null){
                                callBackHelper.onCallBack(null);
                            }
                        }
//                    finish();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Global.has_device_registered = false;
                    System.out.println(t.getMessage());
                    Log.e("login", "Login Failed : " + t.getMessage());
                }
            }));
        }catch (Exception ex){
            Log.e("RegisterDevice", ex.getMessage());
        }
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

     public static LatLng getAlteredLocation(LatLng position, int radius) {
        double x0 = position.latitude;
        double y0 = position.longitude;
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);
        LatLng returnValue = new LatLng(new_x + x0,y + y0);
        return  returnValue;
    }

    public static String GetImageFileFullPath() {
        return GetFilePath() + "/tempImagefile.jpeg";
    }

    public static String GetFilePath() {
        try {
            File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Global.FILE_PATH_SUFFIX);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
        }catch (Exception ex){
            Log.e("CreateDir", ex.getMessage(), ex);
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + Global.FILE_PATH_SUFFIX;
    }

}


