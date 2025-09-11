package com.example.skyapp.api_config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.skyapp.bo.login.BO_response;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        // Skip adding Authorization header if it's already present (manual auth)
        if (request.header("Authorization") != null) {
            Log.d(TAG, "Authorization header already present, skipping auto-auth");
            return chain.proceed(request);
        }
        
        // Try to get token from login response stored in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
        String loginResponseJson = prefs.getString("login_response", null);
        String accessToken = null;
        
        if (loginResponseJson != null) {
            try {
                BO_response.LoginResponse loginResponse = new Gson().fromJson(loginResponseJson, BO_response.LoginResponse.class);
                if (loginResponse != null) {
                    accessToken = loginResponse.getAccessToken();
                    Log.d(TAG, "Access token retrieved from login response");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing login response", e);
            }
        }
        
        // Fallback: try old secure_prefs location
        if (accessToken == null) {
            SharedPreferences securePrefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE);
            accessToken = securePrefs.getString("access_token", null);
            if (accessToken != null) {
                Log.d(TAG, "Access token retrieved from secure_prefs (fallback)");
            }
        }

        Request.Builder requestBuilder = request.newBuilder();
        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            Log.d(TAG, "Added Authorization header to request for: " + request.url());
        } else {
            Log.w(TAG, "No access token available for request: " + request.url());
        }

        return chain.proceed(requestBuilder.build());
    }
}
