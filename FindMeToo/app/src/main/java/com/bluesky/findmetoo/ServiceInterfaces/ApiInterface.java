package com.bluesky.findmetoo.ServiceInterfaces;

import com.bluesky.findmetoo.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
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
    @POST("JoinServer/Location")
    Call<Void> postCurrentLocation(@Header("Authorization") String authorization, @Body CurrentActivity location);
    @POST("JoinServer/Activity")
    Call<Void> postActivity(@Header("Authorization") String authorization, @Body ActivityModel activity);

//    @GET("movie/{id}")
//    Call<MoviesResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);
}