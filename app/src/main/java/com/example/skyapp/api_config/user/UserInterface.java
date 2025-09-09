package com.example.skyapp.api_config.user;

import com.example.skyapp.bo.user.BO_request;
import com.example.skyapp.bo.user.BO_response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UserInterface {

    @POST("/api/users/getUserData")
    Call<BO_response.ProfileResponse> getUserData(
            @Header("Authorization") String bearerToken,
            @Body BO_request.ProfileRequest request
    );
}
