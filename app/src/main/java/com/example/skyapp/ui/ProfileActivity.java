package com.example.skyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skyapp.R;
import com.example.skyapp.api_config.client;
import com.example.skyapp.api_config.ApiService;
import com.example.skyapp.api_config.user.UserInterface;
import com.example.skyapp.bo.login.BO_response;
import com.example.skyapp.bo.user.BO_request;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    // UI Components
    private CircularProgressIndicator progressIndicator;
    private LinearLayout errorContainer;
    private LinearLayout profileContainer;
    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvUserUuid;
    private TextView tvUserId;
    private TextView tvDateCreated;
    private TextView tvErrorMessage;
    private MaterialButton btnRetry;
    private MaterialButton btnRefreshProfile;
    private MaterialButton btnBack;

    // Data
    private String accessToken;
    private int userId;
    private String userUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupClickListeners();
        loadLoginData();
        loadUserProfile();
    }

    private void initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator);
        errorContainer = findViewById(R.id.errorContainer);
        profileContainer = findViewById(R.id.profileContainer);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvUserUuid = findViewById(R.id.tvUserUuid);
        tvUserId = findViewById(R.id.tvUserId);
        tvDateCreated = findViewById(R.id.tvDateCreated);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);
        btnRefreshProfile = findViewById(R.id.btnRefreshProfile);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnRetry.setOnClickListener(v -> loadUserProfile());
        btnRefreshProfile.setOnClickListener(v -> loadUserProfile());
        btnBack.setOnClickListener(v -> finish());
        
        // Setup navigation
        NavigationHelper.setupNavigation(this, ProfileActivity.class);
        NavigationHelper.highlightCurrentSection(this, ProfileActivity.class);
    }

    private void loadLoginData() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
            String loginResponseJson = sharedPreferences.getString("login_response", null);

            if (loginResponseJson != null) {
                BO_response.LoginResponse loginResponse = new Gson().fromJson(loginResponseJson, BO_response.LoginResponse.class);

                if (loginResponse != null) {
                    accessToken = loginResponse.getAccessToken();
                    userId = loginResponse.getUserId();
                    userUuid = loginResponse.getUserUuid();

                    Log.d("ProfileActivity", "Login data loaded - UserId: " + userId + ", UserUuid: " + userUuid);
                } else {
                    showError("Failed to parse login data");
                }
            } else {
                showError("No login data found");
            }
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error loading login data", e);
            showError("Error loading login data: " + e.getMessage());
        }
    }

    private void loadUserProfile() {
        if (accessToken == null || userId == -1 || userUuid == null) {
            showError("Missing login credentials. Please login again.");
            return;
        }

        showLoading();

        // Create the request object
        BO_request.ProfileRequest request = new BO_request.ProfileRequest(
            userId,
            userUuid,
            "83d6661f-9f64-43c4-b672-cdcab3a57685"  // App key from MainActivity
        );

        // Log the request
        Log.d("ProfileActivity", "Making API request with userId: " + userId + ", userUuid: " + userUuid);
        Log.d("ProfileActivity", "Request JSON: " + new Gson().toJson(request));

        // Create API service using USERS service
        UserInterface apiService = client.createService(this, ApiService.USERS, UserInterface.class);

        // Make API call
        Call<com.example.skyapp.bo.user.BO_response.ProfileResponse> call = 
            apiService.getUserData("Bearer " + accessToken, request);

        Log.d("ProfileActivity", "Request URL: " + call.request().url().toString());

        call.enqueue(new Callback<com.example.skyapp.bo.user.BO_response.ProfileResponse>() {
            @Override
            public void onResponse(Call<com.example.skyapp.bo.user.BO_response.ProfileResponse> call, 
                                 Response<com.example.skyapp.bo.user.BO_response.ProfileResponse> response) {

                Log.d("ProfileActivity", "API Response - Code: " + response.code() + ", Message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ProfileActivity", "Response Body: " + new Gson().toJson(response.body()));
                    displayUserData(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("ProfileActivity", "API Error: " + errorBody);
                        showError("Failed to load profile: " + response.code() + " " + response.message());
                    } catch (Exception e) {
                        Log.e("ProfileActivity", "Error reading error body", e);
                        showError("Failed to load profile: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.user.BO_response.ProfileResponse> call, Throwable t) {
                Log.e("ProfileActivity", "API Call failed", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displayUserData(com.example.skyapp.bo.user.BO_response.ProfileResponse profileData) {
        try {
            // Extract data using the helper methods from BO_response
            String fullName = profileData.getFullName();
            String email = profileData.getEmail();
            String userUuidFromApi = profileData.getUserUuid();
            String dateCreated = profileData.getDateCreated();

            // Update UI
            tvFullName.setText(fullName != null ? fullName : "Name not available");
            tvEmail.setText(email != null ? email : "Email not available");
            tvUserUuid.setText(userUuidFromApi != null ? userUuidFromApi : userUuid);
            tvUserId.setText(String.valueOf(userId));

            // Format date if available
            if (dateCreated != null) {
                try {
                    // Parse the date and format it nicely
                    // Assuming the date comes in ISO format, adjust if needed
                    tvDateCreated.setText(formatDate(dateCreated));
                } catch (Exception e) {
                    Log.w("ProfileActivity", "Error formatting date: " + dateCreated, e);
                    tvDateCreated.setText(dateCreated);
                }
            } else {
                tvDateCreated.setText("Date not available");
            }

            showProfile();
            Log.d(TAG, "Profile loaded successfully");

        } catch (Exception e) {
            Log.e("ProfileActivity", "Error displaying user data", e);
            showError("Error displaying profile data");
        }
    }

    private String formatDate(String dateString) {
        try {
            // Try common date formats
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            try {
                // Try alternative format
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (Exception e2) {
                // Return original string if parsing fails
                return dateString;
            }
        }
    }

    private void showLoading() {
        progressIndicator.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
        profileContainer.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressIndicator.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        profileContainer.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
    }

    private void showProfile() {
        progressIndicator.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        profileContainer.setVisibility(View.VISIBLE);
    }
}
