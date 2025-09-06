package com.example.skyapp.api_config;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);

        Request.Builder requestBuilder = chain.request().newBuilder();
        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }

        return chain.proceed(requestBuilder.build());
    }
}
