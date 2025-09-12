package com.example.skyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.example.skyapp.bo.routing.BO_request_extensions;
import com.example.skyapp.realm.DeliveryPackageRealm;
import com.example.skyapp.realm.RealmManager;
// Navigation helper will be used with full path
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import io.realm.RealmResults;

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
    private MaterialButton btnViewReports;
    private MaterialButton btnSortPackages;
    private RecyclerView recyclerViewPackages;
    private TextView txtTotalPackages, txtInTransitPackages, txtDeliveredPackages;
    private MaterialCardView statsCard, packagesCard;
    
    // Data
    private String accessToken;
    private int userId;
    private String userUuid;
    private String currentRouteId;
    private List<com.example.skyapp.bo.routing.BO_response.RoutePointData> allPackages;
    private PackageAdapter packageAdapter;
    private RealmManager realmManager;
    
    // Enhanced package data class to include consignee information
    private static class EnhancedPackageData {
        com.example.skyapp.bo.routing.BO_response.RoutePointData packageData;
        com.example.skyapp.bo.routing.BO_response.Consignee consignee;
        Integer deliveryOrder;
    }

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
        btnViewReports = findViewById(R.id.btnViewReports);
        btnSortPackages = findViewById(R.id.btnSortPackages);
        
        // Stats TextViews
        txtTotalPackages = findViewById(R.id.txtTotalPackages);
        txtInTransitPackages = findViewById(R.id.txtInTransitPackages);
        txtDeliveredPackages = findViewById(R.id.txtDeliveredPackages);
        
        // RecyclerView for packages
        recyclerViewPackages = findViewById(R.id.recyclerViewPackages);
        statsCard = findViewById(R.id.statsCard);
        packagesCard = findViewById(R.id.packagesCard);
        
        // Initialize RealmManager
        realmManager = RealmManager.getInstance(this);
    }

    private void setupClickListeners() {
        // Track Shipments - Show package list
        btnTrackShipments.setOnClickListener(v -> {
            if (packagesCard.getVisibility() == View.VISIBLE) {
                packagesCard.setVisibility(View.GONE);
                btnTrackShipments.setText("Show Package List");
            } else {
                packagesCard.setVisibility(View.VISIBLE);
                btnTrackShipments.setText("Hide Package List");
            }
        });
        
        btnViewReports.setOnClickListener(v -> {
            loadPackageData(); // Refresh data
            // Refreshing package data silently
        });

        btnSortPackages.setOnClickListener(v -> {
            showSortingOptions();
        });

        // Setup navigation
        com.example.skyapp.ui.NavigationHelper.setupNavigation(this, ShipmentsActivity.class);
        com.example.skyapp.ui.NavigationHelper.highlightCurrentSection(this, ShipmentsActivity.class);
    }
    
    private void loadLoginData() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
            String loginResponseJson = sharedPreferences.getString("login_response", null);

            Log.d(TAG, "Loading login data - SharedPrefs found: " + (loginResponseJson != null));

            if (loginResponseJson != null) {
                BO_response.LoginResponse loginResponse = new Gson().fromJson(loginResponseJson, BO_response.LoginResponse.class);

                if (loginResponse != null) {
                    accessToken = loginResponse.getAccessToken();
                    userId = loginResponse.getUserId();
                    userUuid = loginResponse.getUserUuid();
                    currentRouteId = "route_" + userId + "_" + System.currentTimeMillis() / (1000 * 60 * 60 * 24); // Daily route ID

                    Log.d(TAG, "Login data loaded - UserId: " + userId + ", UserUuid: " + userUuid + 
                        ", AccessToken: " + (accessToken != null ? "present" : "null"));
                } else {
                    Log.e(TAG, "LoginResponse object is null");
                    Log.d(TAG, "No valid response data");
                }
            } else {
                Log.e(TAG, "No login data found in SharedPreferences");
                Log.d(TAG, "No login data found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading login data", e);
            Log.e(TAG, "Error loading login data");
        }
    }
    
    private void setupRecyclerView() {
        packageAdapter = new PackageAdapter();
        recyclerViewPackages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPackages.setAdapter(packageAdapter);
        packagesCard.setVisibility(View.GONE);
    }
    
    private void loadPackageData() {
        Log.d(TAG, "loadPackageData() called");
        
        if (accessToken == null || userUuid == null) {
            Log.e(TAG, "Login data not available - accessToken: " + (accessToken != null ? "present" : "null") + 
                ", userUuid: " + (userUuid != null ? "present" : "null"));
            Log.d(TAG, "Login data not available");
            return;
        }

        // Loading package data silently
        Log.d(TAG, "Starting API calls with userId: " + userId + ", userUuid: " + userUuid);

        // Create request objects
        BO_request.User user = new BO_request.User(userId, userUuid);
        BO_request.Application application = new BO_request.Application("83d6661f-9f64-43c4-b672-cdcab3a57685");
        BO_request.UserInfo userInfo = new BO_request.UserInfo(user, application);

        // Get current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(new Date());
        Log.d(TAG, "Using date for API request: " + currentDate);
        
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
                    Log.d(TAG, "API Response successful - Routes found: " + (routes != null ? routes.size() : 0));
                    
                    if (routes != null && !routes.isEmpty()) {
                        Log.d(TAG, "Loading details for routeId: " + routes.get(0).getRouteId());
                        // Load details for the first route
                        loadRouteDetails(routes.get(0).getRouteId(), userInfo);
                    } else {
                        Log.w(TAG, "No routes found in response");
                        Log.d(TAG, "No routes found for today");
                        // Update UI with empty state
                        updateStats();
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error body", e);
                        }
                    }
                    Log.e(TAG, "Failed to load routes");
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading routes");
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
                    Log.d(TAG, "Route details loaded successfully");
                    processPackageData(response.body());
                } else {
                    Log.e(TAG, "Route Details API Error: " + response.code() + " - " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Route Details Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read route details error body", e);
                        }
                    }
                    Log.e(TAG, "Failed to load package details");
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading package details");
                Log.e(TAG, "Route Details API Call failed", t);
            }
        });
    }
    
    
    private void processPackageData(com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse response) {
        try {
            allPackages.clear();
            
            Log.d(TAG, "Processing package data from response");
            
            if (response.getData() == null) {
                Log.e(TAG, "Response data is null");
                Log.d(TAG, "No data received from server");
                return;
            }
            
            List<com.example.skyapp.bo.routing.BO_response.RouteDetail> routes = response.getData().getRoute();
            Log.d(TAG, "Route details found: " + (routes != null ? routes.size() : 0));
            
            if (routes != null && !routes.isEmpty()) {
                com.example.skyapp.bo.routing.BO_response.RouteDetail routeDetail = routes.get(0);
                
                if (routeDetail.getData() == null || routeDetail.getData().getMainInfo() == null) {
                    Log.e(TAG, "Route detail main info is null");
                    Log.e(TAG, "Invalid route data received");
                    return;
                }
                
                com.example.skyapp.bo.routing.BO_response.MainInfo mainInfo = routeDetail.getData().getMainInfo();
                
                // Create enhanced package data with consignee information
                List<EnhancedPackageData> enhancedPackages = new ArrayList<>();
                
                Log.d(TAG, "Route points available: " + (mainInfo.getRoutePoints() != null ? mainInfo.getRoutePoints().size() : 0));
                
                if (mainInfo.getRoutePoints() != null) {
                    int routePointIndex = 0;
                    for (com.example.skyapp.bo.routing.BO_response.RoutePoint routePoint : mainInfo.getRoutePoints()) {
                        Log.d(TAG, "Processing route point " + routePointIndex + " - RoutePointData available: " + 
                            (routePoint.getRoutePointData() != null ? routePoint.getRoutePointData().size() : 0) +
                            ", Consignee available: " + (routePoint.getConsignee() != null));
                        
                        // Si hay RoutePointData, usar ese approach (original)
                        if (routePoint.getRoutePointData() != null && !routePoint.getRoutePointData().isEmpty()) {
                            for (com.example.skyapp.bo.routing.BO_response.RoutePointData packageData : routePoint.getRoutePointData()) {
                                EnhancedPackageData enhanced = new EnhancedPackageData();
                                enhanced.packageData = packageData;
                                enhanced.consignee = routePoint.getConsignee();
                                enhanced.deliveryOrder = routePoint.getDeliveryOrder();
                                enhancedPackages.add(enhanced);
                                
                                Log.d(TAG, "Added package from RoutePointData: " + packageData.getTrackingNumber() + 
                                    ", Consignee: " + (routePoint.getConsignee() != null ? routePoint.getConsignee().getName() : "null"));
                            }
                        } 
                        // Si no hay RoutePointData pero hay consignee, crear un paquete virtual desde el routePoint
                        else if (routePoint.getConsignee() != null) {
                            // Crear un RoutePointData virtual desde la información del routePoint
                            com.example.skyapp.bo.routing.BO_response.RoutePointData virtualPackageData = 
                                new com.example.skyapp.bo.routing.BO_response.RoutePointData();
                            
                            // Usar el routePointId como tracking number si no hay otro
                            virtualPackageData.setTrackingNumber("RP-" + routePoint.getRoutePointId());
                            virtualPackageData.setContainer("Route Point " + routePointIndex);
                            virtualPackageData.setDeliveryStatus(1); // En tránsito por defecto
                            virtualPackageData.setDeliveryStatusDescription("In Transit");
                            
                            EnhancedPackageData enhanced = new EnhancedPackageData();
                            enhanced.packageData = virtualPackageData;
                            enhanced.consignee = routePoint.getConsignee();
                            enhanced.deliveryOrder = routePoint.getDeliveryOrder();
                            enhancedPackages.add(enhanced);
                            
                            Log.d(TAG, "Added virtual package from RoutePoint: " + virtualPackageData.getTrackingNumber() + 
                                ", Consignee: " + routePoint.getConsignee().getName());
                        }
                        routePointIndex++;
                    }
                } else {
                    Log.w(TAG, "No route points found in main info");
                }
                
                // Sort by delivery order
                enhancedPackages.sort((p1, p2) -> {
                    Integer order1 = p1.deliveryOrder;
                    Integer order2 = p2.deliveryOrder;
                    if (order1 == null) order1 = Integer.MAX_VALUE;
                    if (order2 == null) order2 = Integer.MAX_VALUE;
                    return order1.compareTo(order2);
                });
                
                Log.d(TAG, "Loaded " + enhancedPackages.size() + " enhanced packages");
                
                // Update adapter with enhanced data
                packageAdapter.updateData(enhancedPackages);
                
                // Update statistics with original package data for compatibility
                for (EnhancedPackageData enhanced : enhancedPackages) {
                    allPackages.add(enhanced.packageData);
                }
                
                updateStats();
                
                // Save route data to Realm for delivery management
                if (currentRouteId != null && mainInfo != null) {
                    // Create a BO_request_extensions.RouteData object for Realm storage
                    BO_request_extensions.RouteData routeDataForRealm = new BO_request_extensions.RouteData();
                    
                    // Convert route points for Realm storage
                    if (mainInfo.getRoutePoints() != null) {
                        List<BO_request_extensions.RoutePoint> routePointsList = new ArrayList<>();
                        for (com.example.skyapp.bo.routing.BO_response.RoutePoint responsePoint : mainInfo.getRoutePoints()) {
                            BO_request_extensions.RoutePoint requestPoint = new BO_request_extensions.RoutePoint();
                            // Get coordinates from GeoRef
                            if (responsePoint.getGeoRef() != null) {
                                requestPoint.setLatitude(responsePoint.getGeoRef().getLatitude());
                                requestPoint.setLongitude(responsePoint.getGeoRef().getLongitude());
                            } else {
                                // Default coordinates if GeoRef is null
                                requestPoint.setLatitude(0.0);
                                requestPoint.setLongitude(0.0);
                            }
                            
                            // Convert consignee
                            if (responsePoint.getConsignee() != null) {
                                BO_request_extensions.RoutePoint.Consignee consignee = new BO_request_extensions.RoutePoint.Consignee();
                                consignee.setName(responsePoint.getConsignee().getName());
                                consignee.setPhone(responsePoint.getConsignee().getPhone());
                                consignee.setAddress(responsePoint.getConsignee().getAddress());
                                requestPoint.setConsignee(consignee);
                            }
                            
                            routePointsList.add(requestPoint);
                        }
                        routeDataForRealm.setRoutePoints(routePointsList);
                        
                        // Save to Realm
                        realmManager.saveRouteData(currentRouteId, routeDataForRealm);
                        Log.d(TAG, "Saved route data to Realm with ID: " + currentRouteId);
                    }
                }
                
                Log.d(TAG, "Updated stats - Total: " + allPackages.size());
                Log.d(TAG, "Loaded " + enhancedPackages.size() + " packages");
            } else {
                Log.w(TAG, "No route details found");
                Log.d(TAG, "No route details available");
                updateStats(); // Update with empty data
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing package data", e);
            Log.e(TAG, "Error processing package data");
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
    
    private void showSortingOptions() {
        String[] sortOptions = {
            "Delivery Order (Ascending)",
            "Delivery Order (Descending)", 
            "Tracking Number (A-Z)",
            "Tracking Number (Z-A)",
            "Status (Delivered First)",
            "Status (Pending First)",
            "Customer Name (A-Z)"
        };

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Sort Packages By")
            .setItems(sortOptions, (dialog, which) -> {
                sortPackages(which);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sortPackages(int sortType) {
        if (packageAdapter == null) return;

        List<EnhancedPackageData> currentPackages = packageAdapter.getPackages();
        if (currentPackages == null || currentPackages.isEmpty()) return;

        switch (sortType) {
            case 0: // Delivery Order Ascending
                currentPackages.sort((p1, p2) -> {
                    Integer order1 = p1.deliveryOrder != null ? p1.deliveryOrder : Integer.MAX_VALUE;
                    Integer order2 = p2.deliveryOrder != null ? p2.deliveryOrder : Integer.MAX_VALUE;
                    return order1.compareTo(order2);
                });
                break;
            case 1: // Delivery Order Descending
                currentPackages.sort((p1, p2) -> {
                    Integer order1 = p1.deliveryOrder != null ? p1.deliveryOrder : Integer.MIN_VALUE;
                    Integer order2 = p2.deliveryOrder != null ? p2.deliveryOrder : Integer.MIN_VALUE;
                    return order2.compareTo(order1);
                });
                break;
            case 2: // Tracking Number A-Z
                currentPackages.sort((p1, p2) -> {
                    String track1 = p1.packageData.getTrackingNumber() != null ? p1.packageData.getTrackingNumber() : "";
                    String track2 = p2.packageData.getTrackingNumber() != null ? p2.packageData.getTrackingNumber() : "";
                    return track1.compareTo(track2);
                });
                break;
            case 3: // Tracking Number Z-A
                currentPackages.sort((p1, p2) -> {
                    String track1 = p1.packageData.getTrackingNumber() != null ? p1.packageData.getTrackingNumber() : "";
                    String track2 = p2.packageData.getTrackingNumber() != null ? p2.packageData.getTrackingNumber() : "";
                    return track2.compareTo(track1);
                });
                break;
            case 4: // Status - Delivered First
                currentPackages.sort((p1, p2) -> {
                    String status1 = p1.packageData.getDeliveryStatusDescription() != null ? 
                        p1.packageData.getDeliveryStatusDescription().toLowerCase() : "";
                    String status2 = p2.packageData.getDeliveryStatusDescription() != null ? 
                        p2.packageData.getDeliveryStatusDescription().toLowerCase() : "";
                    
                    boolean delivered1 = status1.contains("delivered") || status1.contains("entregado");
                    boolean delivered2 = status2.contains("delivered") || status2.contains("entregado");
                    
                    if (delivered1 && !delivered2) return -1;
                    if (!delivered1 && delivered2) return 1;
                    return 0;
                });
                break;
            case 5: // Status - Pending First
                currentPackages.sort((p1, p2) -> {
                    String status1 = p1.packageData.getDeliveryStatusDescription() != null ? 
                        p1.packageData.getDeliveryStatusDescription().toLowerCase() : "";
                    String status2 = p2.packageData.getDeliveryStatusDescription() != null ? 
                        p2.packageData.getDeliveryStatusDescription().toLowerCase() : "";
                    
                    boolean pending1 = status1.contains("pending") || status1.contains("pendiente") || 
                                      status1.contains("transit") || status1.contains("transito");
                    boolean pending2 = status2.contains("pending") || status2.contains("pendiente") ||
                                      status2.contains("transit") || status2.contains("transito");
                    
                    if (pending1 && !pending2) return -1;
                    if (!pending1 && pending2) return 1;
                    return 0;
                });
                break;
            case 6: // Customer Name A-Z
                currentPackages.sort((p1, p2) -> {
                    String name1 = (p1.consignee != null && p1.consignee.getName() != null) ? 
                        p1.consignee.getName() : "";
                    String name2 = (p2.consignee != null && p2.consignee.getName() != null) ? 
                        p2.consignee.getName() : "";
                    return name1.compareTo(name2);
                });
                break;
        }

        packageAdapter.updateData(currentPackages);
        // Packages sorted silently
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realmManager != null) {
            realmManager.close();
        }
    }
    
    // Modern Package Adapter for RecyclerView
    private static class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> {
        
        private List<EnhancedPackageData> packages = new ArrayList<>();
        
        public void updateData(List<EnhancedPackageData> newPackages) {
            this.packages = newPackages;
            notifyDataSetChanged();
        }
        
        public List<EnhancedPackageData> getPackages() {
            return packages;
        }
        
        @NonNull
        @Override
        public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package_modern, parent, false);
            return new PackageViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
            EnhancedPackageData enhancedData = packages.get(position);
            holder.bind(enhancedData);
        }
        
        @Override
        public int getItemCount() {
            return packages.size();
        }
        
        static class PackageViewHolder extends RecyclerView.ViewHolder {
            
            private TextView txtTrackingNumber, txtContainer, txtCustomerName, txtAddress, txtPhone, txtDeliveryOrder, txtPackageWeight;
            private com.google.android.material.chip.Chip chipStatus;
            private LinearLayout phoneLayout;
            
            public PackageViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTrackingNumber = itemView.findViewById(R.id.txtTrackingNumber);
                txtContainer = itemView.findViewById(R.id.txtContainer);
                chipStatus = itemView.findViewById(R.id.chipStatus);
                txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
                txtAddress = itemView.findViewById(R.id.txtAddress);
                txtPhone = itemView.findViewById(R.id.txtPhone);
                txtDeliveryOrder = itemView.findViewById(R.id.txtDeliveryOrder);
                txtPackageWeight = itemView.findViewById(R.id.txtPackageWeight);
                phoneLayout = itemView.findViewById(R.id.phoneLayout);
            }
            
            public void bind(EnhancedPackageData enhancedData) {
                com.example.skyapp.bo.routing.BO_response.RoutePointData packageData = enhancedData.packageData;
                com.example.skyapp.bo.routing.BO_response.Consignee consignee = enhancedData.consignee;
                
                // Package Information
                txtTrackingNumber.setText(packageData.getTrackingNumber() != null ? 
                    packageData.getTrackingNumber() : "N/A");
                
                txtContainer.setText(packageData.getContainer() != null ? 
                    "Container: " + packageData.getContainer() : "Container: N/A");
                
                // Status Chip
                String status = packageData.getDeliveryStatusDescription() != null ? 
                    packageData.getDeliveryStatusDescription() : "In Transit";
                chipStatus.setText(status);
                
                // Set chip color and style based on status
                Context context = itemView.getContext();
                if (status.toLowerCase().contains("delivered") || status.toLowerCase().contains("entregado")) {
                    chipStatus.setChipBackgroundColorResource(R.color.sepex_green);
                    chipStatus.setTextColor(context.getResources().getColor(android.R.color.white, null));
                } else if (status.toLowerCase().contains("transit") || status.toLowerCase().contains("transito")) {
                    chipStatus.setChipBackgroundColorResource(R.color.sepex_blue);
                    chipStatus.setTextColor(context.getResources().getColor(android.R.color.white, null));
                } else if (status.toLowerCase().contains("pending") || status.toLowerCase().contains("pendiente")) {
                    chipStatus.setChipBackgroundColorResource(R.color.sepex_red);
                    chipStatus.setTextColor(context.getResources().getColor(android.R.color.white, null));
                } else {
                    chipStatus.setChipBackgroundColorResource(R.color.sepex_red);
                    chipStatus.setTextColor(context.getResources().getColor(android.R.color.white, null));
                }
                
                // Customer Information
                if (consignee != null) {
                    txtCustomerName.setText(consignee.getName() != null ? consignee.getName() : "Unknown Customer");
                    
                    // Build full address
                    StringBuilder addressBuilder = new StringBuilder();
                    if (consignee.getAddress() != null) addressBuilder.append(consignee.getAddress());
                    if (consignee.getCity() != null) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(consignee.getCity());
                    }
                    if (consignee.getState() != null) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(consignee.getState());
                    }
                    if (consignee.getZipCode() != null) {
                        if (addressBuilder.length() > 0) addressBuilder.append(" ");
                        addressBuilder.append(consignee.getZipCode());
                    }
                    
                    txtAddress.setText(addressBuilder.length() > 0 ? addressBuilder.toString() : "Address not available");
                    
                    // Phone number
                    if (consignee.getPhone() != null && !consignee.getPhone().trim().isEmpty()) {
                        txtPhone.setText(consignee.getPhone());
                        phoneLayout.setVisibility(View.VISIBLE);
                    } else {
                        phoneLayout.setVisibility(View.GONE);
                    }
                } else {
                    txtCustomerName.setText("Unknown Customer");
                    txtAddress.setText("Address not available");
                    phoneLayout.setVisibility(View.GONE);
                }
                
                // Delivery Order
                txtDeliveryOrder.setText(enhancedData.deliveryOrder != null ? 
                    "#" + enhancedData.deliveryOrder : "#--");
                
                // Package Weight (if available in future API updates)
                // For now, hide the weight field as it's not available in current API
                if (txtPackageWeight != null) {
                    txtPackageWeight.setVisibility(View.GONE);
                }
            }
        }
    }
}