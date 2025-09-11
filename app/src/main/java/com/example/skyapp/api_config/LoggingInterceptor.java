package com.example.skyapp.api_config;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LoggingInterceptor implements Interceptor {
    private static final String TAG = "API_LOG";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        // Log request details
        Log.d(TAG, "========== REQUEST ==========");
        Log.d(TAG, "URL: " + request.url());
        Log.d(TAG, "Method: " + request.method());
        Log.d(TAG, "Headers: " + request.headers());
        
        // Log request body if present
        if (request.body() != null) {
            try {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                Log.d(TAG, "Request Body: " + buffer.readUtf8());
            } catch (Exception e) {
                Log.e(TAG, "Error reading request body", e);
            }
        }

        // Proceed with request
        long startTime = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long endTime = System.currentTimeMillis();
        
        // Log response details
        Log.d(TAG, "========== RESPONSE ==========");
        Log.d(TAG, "URL: " + response.request().url());
        Log.d(TAG, "Status Code: " + response.code());
        Log.d(TAG, "Status Message: " + response.message());
        Log.d(TAG, "Response Time: " + (endTime - startTime) + "ms");
        Log.d(TAG, "Headers: " + response.headers());
        
        // Log response body
        if (response.body() != null) {
            try {
                String responseBodyString = response.body().string();
                Log.d(TAG, "Response Body: " + responseBodyString);
                
                // Create new response with the same body content
                ResponseBody newResponseBody = ResponseBody.create(
                    response.body().contentType(),
                    responseBodyString
                );
                return response.newBuilder().body(newResponseBody).build();
            } catch (Exception e) {
                Log.e(TAG, "Error reading response body", e);
            }
        }
        
        Log.d(TAG, "==============================");
        return response;
    }
}