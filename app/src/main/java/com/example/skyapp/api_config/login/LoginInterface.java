package com.example.skyapp.api_config.login;



import com.example.skyapp.bo.login.BO_request;
import com.example.skyapp.bo.login.BO_response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginInterface {
    @POST("/api/login/loginUser")
    Call<BO_response.LoginResponse> login(@Body BO_request.LoginRequest request);

    @POST("/api/Login/refreshUserToken")
    Call<BO_response.LoginResponse> refreshToken(@Header("Authorization") String refreshToken);



}
