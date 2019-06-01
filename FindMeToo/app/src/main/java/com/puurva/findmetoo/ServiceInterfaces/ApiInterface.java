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
import retrofit2.http.DELETE;
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
    @POST("Notification")
    Call<Void> sendNotification(@Header("Authorization") String authorization, @Body NotificationRequestModel notificationRequestModel);
    @POST("Device")
    Call<Void> postDevice(@Body() DeviceModel device);
    @POST("api/Account/Register")
    Call<Void> registerExternal(@Body() RegisterBindingModel activity);
    @POST("api/Account/SetPassword")
    Call<Void> setPassword(@Header("Authorization") String authorization, @Body() SetPasswordBindingModel setPasswordBindingModel);
    @FormUrlEncoded
    @POST("token")
    Call<Token> getToken(@Field("username") String username, @Field("password") String password, @Field("grant_type") String grantType);
    @GET("Activity/{device}/{activity}/{toplat}/{bottomlat}/{leftlng}/{rightlng}/")
    Call<List<CurrentActivity>> getMatchingActivities(@Header("Authorization") String authorization, @Path("device") String device, @Path("activity") String activity,
                                                      @Path("toplat") double topLat, @Path("bottomlat") double bottomLat,
                                                      @Path("leftlng") double leftLng, @Path("rightlng") double rightLng);
    @GET("Activity/{device}/{toplat}/{bottomlat}/{leftlng}/{rightlng}/")
    Call<List<CurrentActivity>> getAllActivities(@Header("Authorization") String authorization, @Path("device") String device,
                                                 @Path("toplat") double topLat, @Path("bottomlat") double bottomLat,
                                                 @Path("leftlng") double leftLng, @Path("rightlng") double rightLng);
    @GET("ActivityById/{activityid}")
    Call<CurrentActivity> getActivityById(@Header("Authorization") String authorization, @Path("activityid") String activityId);
    @GET("Images/{deviceId}/{fileName}")
    Call<ResponseBody> getMatchingImages(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Path("fileName") String fileName);
    @GET("ProfileImage/{deviceId}")
    Call<ResponseBody> getProfileImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId);
    @POST("Location")
    Call<Void> postCurrentLocation(@Header("Authorization") String authorization, @Body CurrentActivity location);
    @POST("Activity")
    Call<ActivityModel> postActivity(@Header("Authorization") String authorization, @Body ActivityModel activity);
    @PUT("Activity")
    Call<ActivityModel> putActivity(@Header("Authorization") String authorization, @Body ActivityModel activity);
    @DELETE("Activity/{activityid}")
    Call<Void> deleteActivity(@Header("Authorization") String authorization, @Path("activityid") String activityId);
    @POST("ActivitySettings")
    Call<Void> postActivityWithSettins(@Header("Authorization") String authorization, @Body ActivityModel activity, @Body ActivitySettingsModel activitySettings);
    @POST("Profile")
    Call<Void> postProfile(@Header("Authorization") String authorization, @Body ProfileModel profile);
    @Multipart
    @POST("ProfileImage/{deviceId}")
    Call<Void> postProfileImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Part MultipartBody.Part image);
    @Multipart
    @POST("images/{deviceId}/{activity}")
    Call<Void> postActivityImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Path("activity") String activity, @Part MultipartBody.Part image);
    @PUT("Profile/{deviceId}")
    Call<Void> putProfile(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Body ProfileModel profile);
    @GET("Profile/{deviceId}")
    Call<ProfileModel> getProfile(@Header("Authorization") String authorization, @Path("deviceId") String deviceId);
    @POST("profilereview")
    Call<Void> postProfileReview(@Header("Authorization") String authorization, @Body ProfileReviewModel profileReviewModel);
    @GET("profilereview/{deviceId}")
    Call<List<ProfileReviewModel>> getProfileReviews(@Header("Authorization") String authorization, @Path("deviceId") String deviceId);
    @GET("MyActivities/{device}")
    Call<List<ActivityModel>> geMyActivities(@Header("Authorization") String authorization, @Path("device") String device);
    @GET("Notification/{deviceid}")
    Call<List<NotificationDetails>> geMyNotifications(@Header("Authorization") String authorization, @Path("deviceid") String device);
    @GET("ActivityById/{activityid}/Subscribers")
    Call<List<ProfileModel>> getActivitySubscribers(@Header("Authorization") String authorization, @Path("activityid") String activityId);
    @DELETE("Notification/{notificationid}")
    Call<Void> deleteNotification(@Header("Authorization") String authorization, @Path("notificationid") String notificationId);
}
