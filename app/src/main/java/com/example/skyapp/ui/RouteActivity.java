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

public class RouteActivity extends AppCompatActivity {

    private static final String TAG = "RouteActivity";
    
    // UI Components
    private MaterialButton btnViewAllRoutes;
    private MaterialButton btnPlanRoute;
    private MaterialButton btnOptimizeRoutes;
    private MaterialButton btnBackToMaps;
    private RecyclerView recyclerViewRoutePoints;
    private TextView txtTotalRoutes, txtActiveRoutes, txtTotalDistance;
    private TextView txtRouteName, txtRouteStatus, txtEstimatedTime;
    
    // Data
    private String accessToken;
    private int userId;
    private String userUuid;
    private List<com.example.skyapp.bo.routing.BO_response.RoutePoint> routePoints;
    private RoutePointAdapter routePointAdapter;
    private com.example.skyapp.bo.routing.BO_response.MainInfo currentRouteInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // Initialize data structures
        routePoints = new ArrayList<>();
        
        // Load login data
        loadLoginData();
        
        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        
        // Load route data
        loadRouteData();
    }

    private void initializeViews() {
        btnViewAllRoutes = findViewById(R.id.btnViewAllRoutes);
        btnPlanRoute = findViewById(R.id.btnPlanRoute);
        btnOptimizeRoutes = findViewById(R.id.btnOptimizeRoutes);
        btnBackToMaps = findViewById(R.id.btnBackToMaps);
        
        // Stats TextViews
        txtTotalRoutes = findViewById(R.id.txtTotalRoutes);
        txtActiveRoutes = findViewById(R.id.txtActiveRoutes);
        txtTotalDistance = findViewById(R.id.txtTotalDistance);
        
        // Route info TextViews
        txtRouteName = findViewById(R.id.txtRouteName);
        txtRouteStatus = findViewById(R.id.txtRouteStatus);
        txtEstimatedTime = findViewById(R.id.txtEstimatedTime);
        
        // RecyclerView for route points
        recyclerViewRoutePoints = findViewById(R.id.recyclerViewRoutePoints);
    }

    private void setupClickListeners() {
        // Show/hide route points list
        btnViewAllRoutes.setOnClickListener(v -> {
            if (recyclerViewRoutePoints.getVisibility() == View.VISIBLE) {
                recyclerViewRoutePoints.setVisibility(View.GONE);
                btnViewAllRoutes.setText("Show Route Points");
            } else {
                recyclerViewRoutePoints.setVisibility(View.VISIBLE);
                btnViewAllRoutes.setText("Hide Route Points");
            }
        });

        btnPlanRoute.setOnClickListener(v -> {
            Toast.makeText(this, "Plan New Route - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnOptimizeRoutes.setOnClickListener(v -> {
            loadRouteData(); // Refresh data
            Toast.makeText(this, "Refreshing route data...", Toast.LENGTH_SHORT).show();
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
        routePointAdapter = new RoutePointAdapter(routePoints);
        recyclerViewRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRoutePoints.setAdapter(routePointAdapter);
        recyclerViewRoutePoints.setVisibility(View.GONE);
    }
    
    private void loadRouteData() {
        if (accessToken == null || userUuid == null) {
            Toast.makeText(this, "Login data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Loading route data...", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(RouteActivity.this, "No routes found for today", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RouteActivity.this, "Failed to load routes", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, Throwable t) {
                Toast.makeText(RouteActivity.this, "Network error loading routes", Toast.LENGTH_SHORT).show();
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
                    processRouteData(response.body());
                } else {
                    Toast.makeText(RouteActivity.this, "Failed to load route details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Route Details API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call, Throwable t) {
                Toast.makeText(RouteActivity.this, "Network error loading route details", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Route Details API Call failed", t);
            }
        });
    }
    
    private void processRouteData(com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse response) {
        try {
            routePoints.clear();
            
            List<com.example.skyapp.bo.routing.BO_response.RouteDetail> routes = response.getData().getRoute();
            
            if (routes != null && !routes.isEmpty()) {
                com.example.skyapp.bo.routing.BO_response.RouteDetail routeDetail = routes.get(0);
                currentRouteInfo = routeDetail.getData().getMainInfo();
                
                // Add initial point
                if (currentRouteInfo.getInitialPoint() != null) {
                    routePoints.add(currentRouteInfo.getInitialPoint());
                }
                
                // Add route points
                if (currentRouteInfo.getRoutePoints() != null) {
                    routePoints.addAll(currentRouteInfo.getRoutePoints());
                }
                
                // Add end point
                if (currentRouteInfo.getEndPoint() != null) {
                    routePoints.add(currentRouteInfo.getEndPoint());
                }
                
                Log.d(TAG, "Loaded " + routePoints.size() + " route points");
                
                // Update UI
                updateRouteInfo();
                updateStats();
                routePointAdapter.notifyDataSetChanged();
                
                Toast.makeText(this, "Route loaded: " + currentRouteInfo.getHubRoute(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing route data", e);
            Toast.makeText(this, "Error processing route data", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateRouteInfo() {
        if (currentRouteInfo != null) {
            txtRouteName.setText(currentRouteInfo.getHubRoute() != null ? 
                currentRouteInfo.getHubRoute() : "Current Route");
            txtRouteStatus.setText("Active");
            
            // Calculate estimated time based on route points
            int estimatedMinutes = routePoints.size() * 15; // 15 minutes per stop
            int hours = estimatedMinutes / 60;
            int minutes = estimatedMinutes % 60;
            txtEstimatedTime.setText(String.format("%dh %dm", hours, minutes));
        }
    }
    
    private void updateStats() {
        // Update route statistics
        txtTotalRoutes.setText("1"); // Currently showing one route
        txtActiveRoutes.setText("1"); // Active route
        
        // Calculate approximate distance (rough estimation)
        double totalDistance = routePoints.size() * 2.5; // Rough estimate of 2.5 km between points
        txtTotalDistance.setText(String.format("%.1f", totalDistance));
    }
    
    // Route Point Adapter for RecyclerView
    private static class RoutePointAdapter extends RecyclerView.Adapter<RoutePointAdapter.RoutePointViewHolder> {
        
        private List<com.example.skyapp.bo.routing.BO_response.RoutePoint> routePoints;
        
        public RoutePointAdapter(List<com.example.skyapp.bo.routing.BO_response.RoutePoint> routePoints) {
            this.routePoints = routePoints;
        }
        
        @NonNull
        @Override
        public RoutePointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_point, parent, false);
            return new RoutePointViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull RoutePointViewHolder holder, int position) {
            com.example.skyapp.bo.routing.BO_response.RoutePoint routePoint = routePoints.get(position);
            holder.bind(routePoint, position);
        }
        
        @Override
        public int getItemCount() {
            return routePoints.size();
        }
        
        static class RoutePointViewHolder extends RecyclerView.ViewHolder {
            
            private TextView txtStopNumber, txtLocation, txtPackageCount, txtCoordinates;
            
            public RoutePointViewHolder(@NonNull View itemView) {
                super(itemView);
                txtStopNumber = itemView.findViewById(R.id.txtStopNumber);
                txtLocation = itemView.findViewById(R.id.txtLocation);
                txtPackageCount = itemView.findViewById(R.id.txtPackageCount);
                txtCoordinates = itemView.findViewById(R.id.txtCoordinates);
            }
            
            public void bind(com.example.skyapp.bo.routing.BO_response.RoutePoint routePoint, int position) {
                txtStopNumber.setText("Stop " + (position + 1));
                
                if (routePoint.getConsignee() != null) {
                    txtLocation.setText(routePoint.getConsignee().getName());
                } else {
                    txtLocation.setText("Delivery Point");
                }
                
                int packageCount = routePoint.getRoutePointData() != null ? 
                    routePoint.getRoutePointData().size() : 0;
                txtPackageCount.setText(packageCount + " packages");
                
                if (routePoint.getGeoRef() != null) {
                    txtCoordinates.setText(String.format("%.6f, %.6f", 
                        routePoint.getGeoRef().getLatitude(), 
                        routePoint.getGeoRef().getLongitude()));
                } else {
                    txtCoordinates.setText("No coordinates");
                }
            }
        }
    }
}