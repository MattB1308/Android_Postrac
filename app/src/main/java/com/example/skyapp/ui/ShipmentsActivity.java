package com.example.skyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipmentsActivity extends AppCompatActivity {

    private static final String TAG = "ShipmentsActivity";
    
    // UI Components
    private MaterialButton btnTrackShipments;
    private MaterialButton btnCreateShipment;
    private MaterialButton btnViewReports;
    private MaterialButton btnBackToMaps;
    private RecyclerView recyclerViewPackages;
    private TextView txtTotalPackages, txtInTransitPackages, txtDeliveredPackages;
    private MaterialCardView statsCard;
    
    // Data
    private String accessToken;
    private int userId;
    private String userUuid;
    private List<com.example.skyapp.bo.routing.BO_response.RoutePointData> allPackages;
    private PackageAdapter packageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipments);

        // Initialize data structures
        allPackages = new ArrayList<>();
        
        // Load login data
        loadLoginData();
        
        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        
        // Load package data
        loadPackageData();
    }

    private void initializeViews() {
        btnTrackShipments = findViewById(R.id.btnTrackShipments);
        btnCreateShipment = findViewById(R.id.btnCreateShipment);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnBackToMaps = findViewById(R.id.btnBackToMaps);
        
        // Stats TextViews
        txtTotalPackages = findViewById(R.id.txtTotalPackages);
        txtInTransitPackages = findViewById(R.id.txtInTransitPackages);
        txtDeliveredPackages = findViewById(R.id.txtDeliveredPackages);
        
        // RecyclerView for packages
        recyclerViewPackages = findViewById(R.id.recyclerViewPackages);
        statsCard = findViewById(R.id.statsCard);
    }

    private void setupClickListeners() {
        // Track Shipments - Show package list
        btnTrackShipments.setOnClickListener(v -> {
            if (recyclerViewPackages.getVisibility() == View.VISIBLE) {
                recyclerViewPackages.setVisibility(View.GONE);
                btnTrackShipments.setText("Show Package List");
            } else {
                recyclerViewPackages.setVisibility(View.VISIBLE);
                btnTrackShipments.setText("Hide Package List");
            }
        });

        btnCreateShipment.setOnClickListener(v -> {
            Toast.makeText(this, "Create New Shipment - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnViewReports.setOnClickListener(v -> {
            loadPackageData(); // Refresh data
            Toast.makeText(this, "Refreshing package data...", Toast.LENGTH_SHORT).show();
        });

        // Back to Maps functionality
        btnBackToMaps.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
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
    
    private void setupRecyclerView() {
        packageAdapter = new PackageAdapter(allPackages);
        recyclerViewPackages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPackages.setAdapter(packageAdapter);
        recyclerViewPackages.setVisibility(View.GONE);
    }
    
    private void loadPackageData() {
        if (accessToken == null || userUuid == null) {
            Toast.makeText(this, "Login data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Loading package data...", Toast.LENGTH_SHORT).show();

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
                    } else {
                        Toast.makeText(ShipmentsActivity.this, "No routes found for today", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShipmentsActivity.this, "Failed to load routes", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, Throwable t) {
                Toast.makeText(ShipmentsActivity.this, "Network error loading routes", Toast.LENGTH_SHORT).show();
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
                } else {
                    Toast.makeText(ShipmentsActivity.this, "Failed to load package details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Route Details API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call, Throwable t) {
                Toast.makeText(ShipmentsActivity.this, "Network error loading package details", Toast.LENGTH_SHORT).show();
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
                
                Log.d(TAG, "Loaded " + allPackages.size() + " packages");
                
                // Update UI
                updateStats();
                packageAdapter.notifyDataSetChanged();
                
                Toast.makeText(this, "Loaded " + allPackages.size() + " packages", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing package data", e);
            Toast.makeText(this, "Error processing package data", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateStats() {
        int totalPackages = allPackages.size();
        int inTransitPackages = 0;
        int deliveredPackages = 0;
        
        for (com.example.skyapp.bo.routing.BO_response.RoutePointData packageData : allPackages) {
            String status = packageData.getDeliveryStatusDescription();
            if (status != null) {
                if (status.toLowerCase().contains("delivered") || status.toLowerCase().contains("entregado")) {
                    deliveredPackages++;
                } else {
                    inTransitPackages++;
                }
            } else {
                inTransitPackages++;
            }
        }
        
        // Update UI
        txtTotalPackages.setText(String.valueOf(totalPackages));
        txtInTransitPackages.setText(String.valueOf(inTransitPackages));
        txtDeliveredPackages.setText(String.valueOf(deliveredPackages));
    }
    
    // Package Adapter for RecyclerView
    private static class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> {
        
        private List<com.example.skyapp.bo.routing.BO_response.RoutePointData> packages;
        
        public PackageAdapter(List<com.example.skyapp.bo.routing.BO_response.RoutePointData> packages) {
            this.packages = packages;
        }
        
        @NonNull
        @Override
        public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
            return new PackageViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
            com.example.skyapp.bo.routing.BO_response.RoutePointData packageData = packages.get(position);
            holder.bind(packageData);
        }
        
        @Override
        public int getItemCount() {
            return packages.size();
        }
        
        static class PackageViewHolder extends RecyclerView.ViewHolder {
            
            private TextView txtTrackingNumber, txtStatus, txtDestination, txtWeight;
            
            public PackageViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTrackingNumber = itemView.findViewById(R.id.txtTrackingNumber);
                txtStatus = itemView.findViewById(R.id.txtStatus);
                txtDestination = itemView.findViewById(R.id.txtDestination);
                txtWeight = itemView.findViewById(R.id.txtWeight);
            }
            
            public void bind(com.example.skyapp.bo.routing.BO_response.RoutePointData packageData) {
                txtTrackingNumber.setText(packageData.getTrackingNumber() != null ? 
                    packageData.getTrackingNumber() : "N/A");
                    
                txtStatus.setText(packageData.getDeliveryStatusDescription() != null ? 
                    packageData.getDeliveryStatusDescription() : "In Transit");
                    
                txtDestination.setText(packageData.getContainer() != null ? 
                    packageData.getContainer() : "Unknown");
                    
                txtWeight.setText("Status: " + packageData.getDeliveryStatus());
            }
        }
    }
}