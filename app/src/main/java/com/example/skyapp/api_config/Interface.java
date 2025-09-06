package com.example.skyapp.api_config;



import com.example.skyapp.bo.BO_request;
import com.example.skyapp.bo.BO_response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Interface {
    @POST("/api/Login/LoginUser")
    Call<BO_response.LoginResponse> login(@Body BO_request.LoginRequest request);

    @POST("/api/Login/refreshUserToken")
    Call<BO_response.LoginResponse> refreshToken(@Header("Authorization") String refreshToken);



}
