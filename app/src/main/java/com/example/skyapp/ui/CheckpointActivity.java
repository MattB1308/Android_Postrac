package com.example.skyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skyapp.R;
import com.example.skyapp.api_config.ApiService;
import com.example.skyapp.api_config.client;
import com.example.skyapp.api_config.routing.RoutingInterface;
import com.example.skyapp.bo.login.BO_response;
import com.example.skyapp.bo.routing.BO_request;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckpointActivity extends AppCompatActivity {

    private static final String TAG = "CheckpointActivity";
    
    // UI Components
    private MaterialButton btnViewAll;
    private MaterialButton btnCreateNew;
    private MaterialButton btnSearch;
    private MaterialButton btnUpdateCheckpoint;
    
    // Tracking input components
    private TextInputLayout tilTrackingNumber;
    private TextInputEditText etTrackingNumber;
    private TextInputLayout tilCheckpointStatus;
    private AutoCompleteTextView actvCheckpointStatus;
    private MaterialCardView trackingCard;
    
    // Package info display
    private TextView txtPackageInfo, txtDestination, txtCurrentStatus, txtLastUpdate;
    private MaterialCardView packageInfoCard;
    
    // Stats
    private TextView txtTotalCheckpoints, txtActiveCheckpoints, txtCompletedCheckpoints;
    
    // Data
    private String accessToken;
    private int userId;
    private String userUuid;
    private List<com.example.skyapp.bo.routing.BO_response.RoutePointData> allPackages;
    private com.example.skyapp.bo.routing.BO_response.RoutePointData currentPackage;
    
    // Checkpoint status options
    private String[] checkpointStatuses = {
        "DEL - Delivered",
        "CNA - Could Not Access", 
        "CNF - Could Not Find",
        "REF - Refused",
        "DAM - Damaged",
        "RTS - Return to Sender",
        "HLD - Hold",
        "OFD - Out for Delivery",
        "INT - In Transit",
        "ARR - Arrived at Facility"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoint);

        // Initialize data structures
        allPackages = new ArrayList<>();
        
        // Load login data
        loadLoginData();
        
        initializeViews();
        setupClickListeners();
        setupCheckpointStatusDropdown();
        
        // Load package data for tracking
        loadPackageData();
    }

    private void initializeViews() {
        btnViewAll = findViewById(R.id.btnViewAll);
        btnCreateNew = findViewById(R.id.btnCreateNew);
        btnSearch = findViewById(R.id.btnSearch);
        btnUpdateCheckpoint = findViewById(R.id.btnUpdateCheckpoint);
        
        // Tracking input components
        tilTrackingNumber = findViewById(R.id.tilTrackingNumber);
        etTrackingNumber = findViewById(R.id.etTrackingNumber);
        tilCheckpointStatus = findViewById(R.id.tilCheckpointStatus);
        actvCheckpointStatus = findViewById(R.id.actvCheckpointStatus);
        trackingCard = findViewById(R.id.trackingCard);
        
        // Package info components
        txtPackageInfo = findViewById(R.id.txtPackageInfo);
        txtDestination = findViewById(R.id.txtDestination);
        txtCurrentStatus = findViewById(R.id.txtCurrentStatus);
        txtLastUpdate = findViewById(R.id.txtLastUpdate);
        packageInfoCard = findViewById(R.id.packageInfoCard);
        
        // Stats
        txtTotalCheckpoints = findViewById(R.id.txtTotalCheckpoints);
        txtActiveCheckpoints = findViewById(R.id.txtActiveCheckpoints);
        txtCompletedCheckpoints = findViewById(R.id.txtCompletedCheckpoints);
        
        // Initially hide package info
        packageInfoCard.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        // Show/hide tracking functionality
        btnViewAll.setOnClickListener(v -> {
            if (trackingCard.getVisibility() == View.VISIBLE) {
                trackingCard.setVisibility(View.GONE);
                btnViewAll.setText("Track Package");
            } else {
                trackingCard.setVisibility(View.VISIBLE);
                btnViewAll.setText("Hide Tracking");
            }
        });

        btnCreateNew.setOnClickListener(v -> {
            Toast.makeText(this, "Create New Checkpoint - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Search for tracking number  
        btnSearch.setOnClickListener(v -> {
            searchTrackingNumber();
        });
        
        // Search button in tracking card
        MaterialButton btnSearchTracking = findViewById(R.id.btnSearchTracking);
        btnSearchTracking.setOnClickListener(v -> {
            searchTrackingNumber();
        });
        
        // Update checkpoint status
        btnUpdateCheckpoint.setOnClickListener(v -> {
            updateCheckpointStatus();
        });

        // Back to Maps functionality
        // Setup navigation
        NavigationHelper.setupNavigation(this, CheckpointActivity.class);
        NavigationHelper.highlightCurrentSection(this, CheckpointActivity.class);
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

                    Log.d(TAG, "Login data loaded - UserId: " + userId);
                } else {
                    Toast.makeText(this, "No valid response data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No login data found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading login data", e);
            Toast.makeText(this, "Error loading login data", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupCheckpointStatusDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            checkpointStatuses
        );
        actvCheckpointStatus.setAdapter(adapter);
    }
    
    private void loadPackageData() {
        if (accessToken == null || userUuid == null) {
            Toast.makeText(this, "Login data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request objects
        BO_request.User user = new BO_request.User(userId, userUuid);
        BO_request.Application application = new BO_request.Application("83d6661f-9f64-43c4-b672-cdcab3a57685");
        BO_request.UserInfo userInfo = new BO_request.UserInfo(user, application);

        // Get current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(new Date());
        
        BO_request.DeliveryRouteRequest request = new BO_request.DeliveryRouteRequest(userInfo, userId, currentDate);

        // Create API service
        RoutingInterface apiService = client.createService(this, ApiService.ROUTING, RoutingInterface.class);

        // Make API call
        Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call = 
            apiService.getDeliveryRouteByUserAndDate("Bearer " + accessToken, request);

        call.enqueue(new Callback<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse>() {
            @Override
            public void onResponse(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, 
                                 Response<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.skyapp.bo.routing.BO_response.Route> routes = response.body().getRoutes();
                    if (routes != null && !routes.isEmpty()) {
                        // Load details for the first route
                        loadRouteDetails(routes.get(0).getRouteId(), userInfo);
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, Throwable t) {
                Log.e(TAG, "API Call failed", t);
            }
        });
    }
    
    private void loadRouteDetails(int routeId, BO_request.UserInfo userInfo) {
        BO_request.RouteDetailsRequest request = new BO_request.RouteDetailsRequest(userInfo, routeId);

        RoutingInterface apiService = client.createService(this, ApiService.ROUTING, RoutingInterface.class);
        Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call = 
            apiService.getDeliveryRouteInfoByRouteId("Bearer " + accessToken, request);

        call.enqueue(new Callback<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse>() {
            @Override
            public void onResponse(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call,
                                 Response<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    processPackageData(response.body());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call, Throwable t) {
                Log.e(TAG, "Route Details API Call failed", t);
            }
        });
    }
    
    private void processPackageData(com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse response) {
        try {
            allPackages.clear();
            
            List<com.example.skyapp.bo.routing.BO_response.RouteDetail> routes = response.getData().getRoute();
            
            if (routes != null && !routes.isEmpty()) {
                com.example.skyapp.bo.routing.BO_response.RouteDetail routeDetail = routes.get(0);
                com.example.skyapp.bo.routing.BO_response.MainInfo mainInfo = routeDetail.getData().getMainInfo();
                
                // Collect all packages from all route points
                if (mainInfo.getRoutePoints() != null) {
                    for (com.example.skyapp.bo.routing.BO_response.RoutePoint routePoint : mainInfo.getRoutePoints()) {
                        if (routePoint.getRoutePointData() != null) {
                            allPackages.addAll(routePoint.getRoutePointData());
                        }
                    }
                }
                
                // Update stats
                updateStats();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing package data", e);
        }
    }
    
    private void updateStats() {
        int totalCheckpoints = allPackages.size();
        int activeCheckpoints = 0;
        int completedCheckpoints = 0;
        
        for (com.example.skyapp.bo.routing.BO_response.RoutePointData packageData : allPackages) {
            String status = packageData.getDeliveryStatusDescription();
            if (status != null && (status.toLowerCase().contains("delivered") || status.toLowerCase().contains("del"))) {
                completedCheckpoints++;
            } else {
                activeCheckpoints++;
            }
        }
        
        // Update UI
        txtTotalCheckpoints.setText(String.valueOf(totalCheckpoints));
        txtActiveCheckpoints.setText(String.valueOf(activeCheckpoints));
        txtCompletedCheckpoints.setText(String.valueOf(completedCheckpoints));
    }
    
    private void searchTrackingNumber() {
        String trackingNumber = etTrackingNumber.getText() != null ? etTrackingNumber.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(trackingNumber)) {
            tilTrackingNumber.setError("Please enter a tracking number");
            return;
        }
        
        tilTrackingNumber.setError(null);
        
        // Search for the package in loaded data
        currentPackage = null;
        for (com.example.skyapp.bo.routing.BO_response.RoutePointData packageData : allPackages) {
            if (packageData.getTrackingNumber() != null && 
                packageData.getTrackingNumber().equalsIgnoreCase(trackingNumber)) {
                currentPackage = packageData;
                break;
            }
        }
        
        if (currentPackage != null) {
            displayPackageInfo();
            Toast.makeText(this, "Package found!", Toast.LENGTH_SHORT).show();
        } else {
            packageInfoCard.setVisibility(View.GONE);
            Toast.makeText(this, "Tracking number not found in current routes", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void displayPackageInfo() {
        if (currentPackage == null) return;
        
        txtPackageInfo.setText("Tracking: " + currentPackage.getTrackingNumber());
        txtDestination.setText("Container: " + (currentPackage.getContainer() != null ? 
            currentPackage.getContainer() : "Unknown"));
        txtCurrentStatus.setText("Status: " + (currentPackage.getDeliveryStatusDescription() != null ? 
            currentPackage.getDeliveryStatusDescription() : "In Transit"));
        txtLastUpdate.setText("Status Code: " + currentPackage.getDeliveryStatus());
        
        // Set current status in dropdown
        if (currentPackage.getDeliveryStatusDescription() != null) {
            String currentStatus = currentPackage.getDeliveryStatusDescription();
            for (String status : checkpointStatuses) {
                if (status.toLowerCase().contains(currentStatus.toLowerCase())) {
                    actvCheckpointStatus.setText(status, false);
                    break;
                }
            }
        }
        
        packageInfoCard.setVisibility(View.VISIBLE);
    }
    
    private void updateCheckpointStatus() {
        if (currentPackage == null) {
            Toast.makeText(this, "Please search for a package first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String selectedStatus = actvCheckpointStatus.getText().toString();
        if (TextUtils.isEmpty(selectedStatus)) {
            tilCheckpointStatus.setError("Please select a checkpoint status");
            return;
        }
        
        tilCheckpointStatus.setError(null);
        
        // Extract status code (first 3 characters)
        String statusCode = selectedStatus.length() >= 3 ? selectedStatus.substring(0, 3) : selectedStatus;
        
        // In a real implementation, this would make an API call to update the status
        // For now, we'll just update locally and show a message
        
        Toast.makeText(this, "Checkpoint updated: " + statusCode + " for " + currentPackage.getTrackingNumber(), Toast.LENGTH_LONG).show();
        
        // Update the display
        txtCurrentStatus.setText("Status: " + selectedStatus);
        txtLastUpdate.setText("Last Update: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
        
        Log.d(TAG, "Updated checkpoint status to: " + statusCode + " for tracking: " + currentPackage.getTrackingNumber());
    }
}