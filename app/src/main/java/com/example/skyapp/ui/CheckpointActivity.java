package com.example.skyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skyapp.R;
import com.example.skyapp.api_config.ApiService;
import com.example.skyapp.api_config.client;
import com.example.skyapp.api_config.routing.RoutingInterface;
import com.example.skyapp.bo.login.BO_response;
import com.example.skyapp.bo.routing.BO_request;
import com.example.skyapp.realm.DeliveryPackageRealm;
import com.example.skyapp.realm.RealmManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import androidx.appcompat.app.AlertDialog;

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
    
    // Package List
    private RecyclerView recyclerViewPackages;
    private PackageCheckpointAdapter packageAdapter;
    private MaterialCardView packagesListCard;
    
    // Data
    private String accessToken;
    private int userId;
    private String userUuid;
    private String currentRouteId;
    private List<com.example.skyapp.bo.routing.BO_response.RoutePointData> allPackages;
    private com.example.skyapp.bo.routing.BO_response.RoutePointData currentPackage;
    private DeliveryPackageRealm currentRealmPackage;
    private RealmManager realmManager;
    
    // Delivery status options for realm
    private String[] deliveryStatuses = {
        "0 - Pending",
        "1 - Delivered",
        "2 - Exception"
    };
    
    private String[] exceptionReasons = {
        "Could Not Access",
        "Could Not Find",
        "Refused",
        "Damaged Package",
        "Incorrect Address",
        "Customer Not Home",
        "Business Closed",
        "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoint);

        // Initialize data structures
        allPackages = new ArrayList<>();
        realmManager = RealmManager.getInstance(this);
        
        // Load login data
        loadLoginData();
        
        initializeViews();
        setupClickListeners();
        setupDeliveryStatusDropdown();
        
        // Setup RecyclerView
        setupRecyclerView();
        
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
        
        // Package List
        recyclerViewPackages = findViewById(R.id.recyclerViewPackages);
        packagesListCard = findViewById(R.id.packagesListCard);
        
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
            Log.d(TAG, "Create New Checkpoint - Coming Soon");
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
    
    private void setupRecyclerView() {
        packageAdapter = new PackageCheckpointAdapter();
        recyclerViewPackages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPackages.setAdapter(packageAdapter);
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
                    currentRouteId = "route_" + userId + "_" + System.currentTimeMillis() / (1000 * 60 * 60 * 24); // Daily route ID

                    Log.d(TAG, "Login data loaded - UserId: " + userId);
                } else {
                    Log.d(TAG, "No valid response data");
                }
            } else {
                Log.d(TAG, "No login data found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading login data", e);
            Log.e(TAG, "Error loading login data");
        }
    }
    
    private void setupCheckpointStatusDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            deliveryStatuses
        );
        actvCheckpointStatus.setAdapter(adapter);
    }
    
    private void loadPackageData() {
        if (accessToken == null || userUuid == null) {
            Log.d(TAG, "Login data not available");
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
                
                // Load packages in ordered list
                loadOrderedPackages();
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
        
        // Search for package in Realm database first
        currentRealmPackage = realmManager.findPackageByTracking(trackingNumber);
        
        if (currentRealmPackage != null) {
            currentPackage = null; // Clear legacy package
            displayPackageInfo();
            Log.d(TAG, "Package found in Realm: " + currentRealmPackage.getConsigneeName());
        } else {
            // Fallback to searching in loaded data
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
                Log.d(TAG, "Package found in loaded data!");
            } else {
                packageInfoCard.setVisibility(View.GONE);
                Log.d(TAG, "Tracking number not found in current routes");
            }
        }
    }
    
    private void displayPackageInfo() {
        if (currentRealmPackage != null) {
            // Display Realm package info
            txtPackageInfo.setText("Tracking: " + currentRealmPackage.getTrackingNumber());
            txtDestination.setText("Destination: " + currentRealmPackage.getConsigneeAddress());
            txtCurrentStatus.setText("Status: " + currentRealmPackage.getDeliveryStatusText());
            txtLastUpdate.setText("Customer: " + currentRealmPackage.getConsigneeName());
            
            // Set current status in dropdown
            actvCheckpointStatus.setText(currentRealmPackage.getDeliveryStatus() + " - " + 
                currentRealmPackage.getDeliveryStatusText(), false);
                
        } else if (currentPackage != null) {
            // Fallback to display legacy package info
            txtPackageInfo.setText("Tracking: " + currentPackage.getTrackingNumber());
            txtDestination.setText("Container: " + (currentPackage.getContainer() != null ? 
                currentPackage.getContainer() : "Unknown"));
            txtCurrentStatus.setText("Status: " + (currentPackage.getDeliveryStatusDescription() != null ? 
                currentPackage.getDeliveryStatusDescription() : "In Transit"));
            txtLastUpdate.setText("Status Code: " + currentPackage.getDeliveryStatus());
        }
        
        packageInfoCard.setVisibility(View.VISIBLE);
    }
    
    private void updateCheckpointStatus() {
        if (currentRealmPackage == null && currentPackage == null) {
            Log.d(TAG, "Please search for a package first");
            return;
        }
        
        String selectedStatus = actvCheckpointStatus.getText().toString();
        if (TextUtils.isEmpty(selectedStatus)) {
            tilCheckpointStatus.setError("Please select a delivery status");
            return;
        }
        
        tilCheckpointStatus.setError(null);
        
        if (currentRealmPackage != null) {
            // Extract status code (first digit)
            int statusCode = -1;
            String notes = "";
            
            if (selectedStatus.startsWith("0")) {
                statusCode = 0; // Pending
            } else if (selectedStatus.startsWith("1")) {
                statusCode = 1; // Delivered
                notes = "Package delivered successfully";
            } else if (selectedStatus.startsWith("2")) {
                statusCode = 2; // Exception
                // Show exception reason dialog
                showExceptionReasonDialog(currentRealmPackage.getTrackingNumber());
                return;
            }
            
            if (statusCode != -1) {
                // Update in Realm
                boolean updated = realmManager.updateDeliveryStatus(currentRealmPackage.getTrackingNumber(), statusCode, notes);
                
                if (updated) {
                    // Refresh the package info from Realm
                    currentRealmPackage = realmManager.findPackageByTracking(currentRealmPackage.getTrackingNumber());
                    displayPackageInfo();
                    updateDeliveryStats();
                    
                    Log.d(TAG, "Updated delivery status to: " + statusCode + " for tracking: " + currentRealmPackage.getTrackingNumber());
                } else {
                    Log.e(TAG, "Failed to update delivery status");
                }
            }
        } else {
            // Legacy update for non-Realm packages
            Log.d(TAG, "Updated legacy package status");
        
            // Update the display
            txtCurrentStatus.setText("Status: " + selectedStatus);
            txtLastUpdate.setText("Last Update: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
        }
    }
    
    private void showExceptionReasonDialog(String trackingNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exception Reason")
                .setItems(exceptionReasons, (dialog, which) -> {
                    String reason = exceptionReasons[which];
                    // Update with exception status and reason
                    boolean updated = realmManager.updateDeliveryStatus(trackingNumber, 2, reason);
                    
                    if (updated) {
                        // Refresh the package info from Realm
                        currentRealmPackage = realmManager.findPackageByTracking(trackingNumber);
                        displayPackageInfo();
                        updateDeliveryStats();
                        loadOrderedPackages(); // Refresh the list
                        
                        Log.d(TAG, "Updated delivery status to exception with reason: " + reason);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void setupDeliveryStatusDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, deliveryStatuses);
        actvCheckpointStatus.setAdapter(adapter);
    }
    
    private void updateDeliveryStats() {
        if (currentRouteId == null) return;
        
        RealmManager.DeliveryStats stats = realmManager.getDeliveryStats(currentRouteId);
        
        txtTotalCheckpoints.setText(String.valueOf(stats.getTotal()));
        txtActiveCheckpoints.setText(String.valueOf(stats.getPending()));
        txtCompletedCheckpoints.setText(String.valueOf(stats.getDelivered() + stats.getExceptions()));
        
        Log.d(TAG, "Updated delivery stats - Total: " + stats.getTotal() + 
              ", Pending: " + stats.getPending() + 
              ", Delivered: " + stats.getDelivered() + 
              ", Exceptions: " + stats.getExceptions());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realmManager != null) {
            realmManager.close();
        }
    }
    
    // Package Adapter for RecyclerView
    private class PackageCheckpointAdapter extends RecyclerView.Adapter<PackageCheckpointAdapter.PackageViewHolder> {
        
        private List<DeliveryPackageRealm> packages = new ArrayList<>();
        
        public void updatePackages(List<DeliveryPackageRealm> newPackages) {
            this.packages = newPackages;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package_checkpoint, parent, false);
            return new PackageViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
            DeliveryPackageRealm packageRealm = packages.get(position);
            holder.bind(packageRealm);
        }
        
        @Override
        public int getItemCount() {
            return packages.size();
        }
        
        class PackageViewHolder extends RecyclerView.ViewHolder {
            
            private TextView txtTrackingNumber, txtDeliveryOrder, txtStatus, txtConsigneeName;
            private com.google.android.material.chip.Chip chipStatus;
            private MaterialCardView cardContainer;
            
            public PackageViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTrackingNumber = itemView.findViewById(R.id.txtTrackingNumber);
                txtDeliveryOrder = itemView.findViewById(R.id.txtDeliveryOrder);
                txtConsigneeName = itemView.findViewById(R.id.txtConsigneeName);
                chipStatus = itemView.findViewById(R.id.chipStatus);
                cardContainer = itemView.findViewById(R.id.cardContainer);
            }
            
            public void bind(DeliveryPackageRealm packageRealm) {
                txtTrackingNumber.setText(packageRealm.getTrackingNumber());
                txtDeliveryOrder.setText("Order #" + packageRealm.getDeliveryOrder());
                txtConsigneeName.setText(packageRealm.getConsigneeName());
                
                // Set status chip
                String statusText;
                int chipColor;
                switch (packageRealm.getDeliveryStatus()) {
                    case 0:
                        statusText = "Pending";
                        chipColor = R.color.sepex_red;
                        break;
                    case 1:
                        statusText = "Delivered";
                        chipColor = R.color.sepex_green;
                        break;
                    case 2:
                        statusText = "Exception";
                        chipColor = R.color.sepex_blue;
                        break;
                    default:
                        statusText = "Unknown";
                        chipColor = R.color.sepex_blue;
                        break;
                }
                
                chipStatus.setText(statusText);
                chipStatus.setChipBackgroundColorResource(chipColor);
                chipStatus.setTextColor(getResources().getColor(android.R.color.white, null));
                
                // Set click listener for package options
                cardContainer.setOnClickListener(v -> showPackageOptionsDialog(packageRealm));
            }
        }
    }
    
    private void showPackageOptionsDialog(DeliveryPackageRealm packageRealm) {
        String[] options = {"Scan Package", "Mark as Delivered", "Mark as Exception"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Package: " + packageRealm.getTrackingNumber())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Scan Package
                            scanPackage(packageRealm);
                            break;
                        case 1: // Mark as Delivered
                            markAsDelivered(packageRealm);
                            break;
                        case 2: // Mark as Exception
                            markAsException(packageRealm);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void scanPackage(DeliveryPackageRealm packageRealm) {
        Log.d(TAG, "Scan functionality for " + packageRealm.getTrackingNumber() + " - Coming Soon");
        // TODO: Implement barcode/QR scanning
    }
    
    private void markAsDelivered(DeliveryPackageRealm packageRealm) {
        boolean updated = realmManager.updateDeliveryStatus(packageRealm.getTrackingNumber(), 1, "Delivered successfully");
        
        if (updated) {
            loadOrderedPackages(); // Refresh the list
            updateDeliveryStats();
            Log.d(TAG, "Marked package as delivered: " + packageRealm.getTrackingNumber());
        }
    }
    
    private void markAsException(DeliveryPackageRealm packageRealm) {
        showExceptionReasonDialog(packageRealm.getTrackingNumber());
    }
    
    private void loadOrderedPackages() {
        if (currentRouteId == null) {
            Log.w(TAG, "No route ID available for loading packages");
            return;
        }
        
        // Get packages from Realm ordered by delivery order
        io.realm.RealmResults<DeliveryPackageRealm> realmPackages = 
            realmManager.getAllPackagesForRoute(currentRouteId);
        
        List<DeliveryPackageRealm> packagesList = new ArrayList<>();
        for (DeliveryPackageRealm pkg : realmPackages) {
            packagesList.add(pkg);
        }
        
        // Update adapter
        packageAdapter.updatePackages(packagesList);
        
        Log.d(TAG, "Loaded " + packagesList.size() + " packages ordered by delivery order");
    }
}