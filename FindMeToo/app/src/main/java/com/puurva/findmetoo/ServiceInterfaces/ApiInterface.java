package com.puurva.findmetoo.ServiceInterfaces;

import com.puurva.findmetoo.model.*;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.*;

public interface ApiInterface {
    @POST("JoinServer/api/Account/Register")
    Call<Void> registerExternal(@Body() RegisterBindingModel activity);
    @FormUrlEncoded
    @POST("JoinServer/token")
    Call<Token> getToken(@Field("username") String username, @Field("password") String password, @Field("grant_type") String grantType);
    @GET("JoinServer/Activity/{activity}")
    Call<List<CurrentActivity>> getMatchingActivities(@Header("Authorization") String authorization, @Path("activity") String activity);
    @GET("JoinServer/Images/{deviceId}/{fileName}")
    Call<ResponseBody> getMatchingImages(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Path("fileName") String fileName);
    @POST("JoinServer/Location")
    Call<Void> postCurrentLocation(@Header("Authorization") String authorization, @Body CurrentActivity location);
    @POST("JoinServer/Activity")
    Call<Void> postActivity(@Header("Authorization") String authorization, @Body ActivityModel activity);
    @POST("JoinServer/Profile")
    Call<Void> postProfile(@Header("Authorization") String authorization, @Body ProfileModel profile);
    @Multipart
    @POST("JoinServer/Profile/{deviceId}")
    Call<Void> postImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Part MultipartBody.Part image);
    @Multipart
    @POST("JoinServer/images/{deviceId}/{activity}")
    Call<Void> postActivityImage(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Path("activity") String activity, @Part MultipartBody.Part image);
    @PUT("JoinServer/Profile/{deviceId}")
    Call<Void> putProfile(@Header("Authorization") String authorization, @Path("deviceId") String deviceId, @Body ProfileModel profile);
    @GET("JoinServer/Profile/{deviceId}")
    Call<ProfileModel> getProfile(@Header("Authorization") String authorization, @Path("deviceId") String deviceId);
}
