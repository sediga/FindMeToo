package com.puurva.findmetoo.ServiceInterfaces;

import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivitySettingsModel;
import com.puurva.findmetoo.ServiceInterfaces.model.CurrentActivity;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.NotificationRequestModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileReviewModel;
import com.puurva.findmetoo.ServiceInterfaces.model.RegisterBindingModel;
import com.puurva.findmetoo.ServiceInterfaces.model.Token;
import com.puurva.findmetoo.ServiceInterfaces.model.*;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.*;

public interface ApiInterface {
    @POST("JoinServer/Notification")
    Call<Void> sendNotification(@Header("Authorization") String authorization, @Body NotificationRequestModel notificationRequestModel);
    @POST("JoinServer/Device")
    Call<Void> postDevice(@Body() DeviceModel device);
    @POST("JoinServer/api/Account/Register")
    Call<Void> registerExternal(@Body() RegisterBindingModel activity);
    @FormUrlEncoded
    @POST("JoinServer/token")
    Call<Token> getToken(@Field("username") String username, @Field("password") String password, @Field("grant_type") String grantType);
    @GET("JoinServer/Activity/{device}/{activity}")
    Call<List<CurrentActivity>> getMatchingActivities(@Header("Authorization") String authorization, @Path("device") String device, @Path("activity") String activity);
    @GET("JoinServer/Activity/{device}")
    Call<List<CurrentActivity>> getAllActivities(@Header("Authorization") String authorization, @Path("device") String device);
    @GET("JoinServer/ActivityById/{activityid}")
    Call<CurrentActivity> getActivityById(@Header("Authorization") String authorization, @Path("activityid") String activityId);
    @GET("JoinServer/Images/{deviceId}/{fileName}")
    Call<ResponseBody> getMatchingImages(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Path("fileName") String fileName);
    @GET("JoinServer/ProfileImage/{deviceId}")
    Call<ResponseBody> getProfileImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId);
    @POST("JoinServer/Location")
    Call<Void> postCurrentLocation(@Header("Authorization") String authorization, @Body CurrentActivity location);
    @POST("JoinServer/Activity")
    Call<ActivityModel> postActivity(@Header("Authorization") String authorization, @Body ActivityModel activity);
    @POST("JoinServer/ActivitySettings")
    Call<Void> postActivityWithSettins(@Header("Authorization") String authorization, @Body ActivityModel activity, @Body ActivitySettingsModel activitySettings);
    @POST("JoinServer/Profile")
    Call<Void> postProfile(@Header("Authorization") String authorization, @Body ProfileModel profile);
    @Multipart
    @POST("JoinServer/ProfileImage/{deviceId}")
    Call<Void> postProfileImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Part MultipartBody.Part image);
    @Multipart
    @POST("JoinServer/images/{deviceId}/{activity}")
    Call<Void> postActivityImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Path("activity") String activity, @Part MultipartBody.Part image);
    @PUT("JoinServer/Profile/{deviceId}")
    Call<Void> putProfile(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Body ProfileModel profile);
    @GET("JoinServer/Profile/{deviceId}")
    Call<ProfileModel> getProfile(@Header("Authorization") String authorization, @Path("deviceId") String deviceId);
    @POST("JoinServer/profilereview")
    Call<Void> postProfileReview(@Header("Authorization") String authorization, @Body ProfileReviewModel profileReviewModel);
    @GET("JoinServer/profilereview/{deviceId}")
    Call<List<ProfileReviewModel>> getProfileReviews(@Header("Authorization") String authorization, @Path("deviceId") String deviceId);
}
