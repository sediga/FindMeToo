package com.bluesky.findmetoo.ServiceInterfaces;

import com.bluesky.findmetoo.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.*;

public interface ApiInterface {
    @GET("JoinServer/Activity/{activity}")
    Call<List<CurrentActivity>> getMatchingActivities(@Path("activity") String activity);
    @POST("JoinServer/api/Account/RegisterExternal")
    Call<Token> registerExternal(@Body() RegisterBindingModel activity);

//    @GET("movie/{id}")
//    Call<MoviesResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);
}
