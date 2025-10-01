package com.example.skyapp.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.skyapp.LoadingActivity;
import com.example.skyapp.R;
import com.example.skyapp.api_config.ApiService;
import com.example.skyapp.api_config.client;
import com.example.skyapp.api_config.routing.RoutingInterface;
import com.example.skyapp.bo.login.BO_response;
import com.example.skyapp.bo.routing.BO_request;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.skyapp.databinding.ActivityMapsBinding;
import com.example.skyapp.realm.DeliveryPackageRealm;
import com.example.skyapp.realm.RealmManager;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    
    // UI Components
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityMapsBinding binding;
    
    // Route Filter Controls
    private MaterialCardView routeFilterCard;
    private MaterialButton btnShowNext1, btnShowNext5, btnShowNext10, btnShowNext20;
    private MaterialButton btnLoadRoutes;
    private FloatingActionButton fabFilter;
    private FloatingActionButton fabUserLocation;
    private FloatingActionButton fabNavigate;
    
    // Data
    private String accessToken;
    private int userId;
    private String userUuid;
    private List<com.example.skyapp.bo.routing.BO_response.RoutePoint> currentRoutePoints;
    private List<Marker> routeMarkers;
    private int currentFilterPoints = 1; // Default: show next 1 point
    private boolean filterCardVisible = false;
    private String currentRouteId;
    private RealmManager realmManager;
    
    // Navigation state for arrival confirmation
    private boolean navigationInProgress = false;
    private String currentNavigationApp = "";
    private String currentDestinationName = "";
    private String currentDestinationAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize data structures
        currentRoutePoints = new ArrayList<>();
        routeMarkers = new ArrayList<>();

        // Load login data
        loadLoginData();

        // Initialize views
        initializeViews();
        setupClickListeners();

        // Inicializar el FusedLocationProviderClient para obtener la ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initializeViews() {
        // Route filter controls
        routeFilterCard = findViewById(R.id.routeFilterCard);
        btnShowNext1 = findViewById(R.id.btnShowNext1);
        btnShowNext5 = findViewById(R.id.btnShowNext5);
        btnShowNext10 = findViewById(R.id.btnShowNext10);
        btnShowNext20 = findViewById(R.id.btnShowNext20);
        btnLoadRoutes = findViewById(R.id.btnLoadRoutes);
        fabFilter = findViewById(R.id.fab_filter);
        fabUserLocation = findViewById(R.id.fab_user_location);
        fabNavigate = findViewById(R.id.fab_navigate);
        
        // Initialize RealmManager
        realmManager = RealmManager.getInstance(this);
    }

    private void setupClickListeners() {
        // Setup navigation
        NavigationHelper.setupNavigation(this, MapsActivity.class);
        NavigationHelper.highlightCurrentSection(this, MapsActivity.class);

        // FAB listeners
        fabUserLocation.setOnClickListener(v -> getCurrentLocation());
        fabFilter.setOnClickListener(v -> toggleFilterCard());
        fabNavigate.setOnClickListener(v -> showNavigationMenu());

        // Filter buttons
        btnShowNext1.setOnClickListener(v -> setFilterPoints(1));
        btnShowNext5.setOnClickListener(v -> setFilterPoints(5));
        btnShowNext10.setOnClickListener(v -> setFilterPoints(10));
        btnShowNext20.setOnClickListener(v -> setFilterPoints(20));
        btnLoadRoutes.setOnClickListener(v -> loadDeliveryRoutes());
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

                    Log.d(TAG, "Login data loaded - UserId: " + userId + ", UserUuid: " + userUuid);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Habilitar el botón de ubicación en el mapa
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no se han concedido
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Show filter card initially
        routeFilterCard.setVisibility(View.VISIBLE);
        filterCardVisible = true;
    }

    // Método para obtener la ubicación actual del dispositivo
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no se han concedido
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Verificar si se ha obtenido una ubicación
                    if (location != null) {
                        // Crear un LatLng con la ubicación obtenida
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        // Colocar un marcador en la ubicación actual
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi Ubicación Actual"));
                        // Mover la cámara al marcador
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    }
                });
    }

    /**
     * Center map on the route origin (initialPoint) or current loaded route
     */
    private void centerOnRoute() {
        if (mMap == null) {
            Log.d(TAG, "Map not ready");
            return;
        }

        if (!currentRoutePoints.isEmpty()) {
            // Focus on the first point (origin/initialPoint)
            com.example.skyapp.bo.routing.BO_response.RoutePoint firstPoint = currentRoutePoints.get(0);
            com.example.skyapp.bo.routing.BO_response.GeoRef geoRef = firstPoint.getGeoRef();
            
            if (geoRef != null) {
                LatLng originLocation = new LatLng(geoRef.getLatitude(), geoRef.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 15));
                Log.d(TAG, "Centered on route origin");
                Log.d(TAG, "Centered map on route origin: " + originLocation);
            } else {
                Log.d(TAG, "Origin location not available");
            }
        } else {
            // Fall back to device location if no route is loaded
            getCurrentLocation();
            Log.d(TAG, "No route loaded, showing device location");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Si se otorgan los permisos, obtener la ubicación
            getCurrentLocation();
        }
    }

    // ========== NEW ROUTE FUNCTIONALITY ==========

    /**
     * Toggle visibility of filter card
     */
    private void toggleFilterCard() {
        if (filterCardVisible) {
            routeFilterCard.setVisibility(View.GONE);
            filterCardVisible = false;
        } else {
            routeFilterCard.setVisibility(View.VISIBLE);
            filterCardVisible = true;
        }
    }

    /**
     * Set the number of points to show and update button states
     */
    private void setFilterPoints(int points) {
        currentFilterPoints = points;
        
        // Update button states
        updateFilterButtonStates();
        
        // Apply filter to current routes if any
        if (!currentRoutePoints.isEmpty()) {
            displayFilteredRoutes();
        }
    }

    /**
     * Update filter button visual states with modern gray/blue theme
     */
    private void updateFilterButtonStates() {
        // Reset all buttons to gray style
        resetFilterButtons();

        // Set selected button to blue style
        MaterialButton selectedButton;
        switch (currentFilterPoints) {
            case 5:
                selectedButton = btnShowNext5;
                break;
            case 10:
                selectedButton = btnShowNext10;
                break;
            case 20:
                selectedButton = btnShowNext20;
                break;
            default:
                selectedButton = btnShowNext1;
                break;
        }

        // Update selected button appearance - blue background with white text
        selectedButton.setBackgroundTintList(getColorStateList(R.color.sepex_blue));
        selectedButton.setTextColor(getColorStateList(R.color.white));
    }

    /**
     * Reset all filter buttons to gray style with white text
     */
    private void resetFilterButtons() {
        MaterialButton[] buttons = {btnShowNext1, btnShowNext5, btnShowNext10, btnShowNext20};
        for (MaterialButton button : buttons) {
            // Set gray background and white text for unselected buttons
            button.setBackgroundTintList(getColorStateList(R.color.button_gray));
            button.setTextColor(getColorStateList(R.color.white));
        }
    }

    /**
     * Load delivery routes from API
     */
    private void loadDeliveryRoutes() {
        Log.d(TAG, "Starting loadDeliveryRoutes()");
        
        if (accessToken == null || userUuid == null) {
            Log.e(TAG, "Missing login data - accessToken: " + (accessToken != null ? "present" : "null") + 
                      ", userUuid: " + (userUuid != null ? "present" : "null"));
            Log.d(TAG, "Login data not available");
            return;
        }

        // Show loading
        // Loading routes silently

        // Create request objects
        BO_request.User user = new BO_request.User(userId, userUuid);
        BO_request.Application application = new BO_request.Application("83d6661f-9f64-43c4-b672-cdcab3a57685");
        BO_request.UserInfo userInfo = new BO_request.UserInfo(user, application);

        // Get current date in required format
        String currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(new Date());
        
        BO_request.DeliveryRouteRequest request = new BO_request.DeliveryRouteRequest(userInfo, userId, currentDate);

        Log.d(TAG, "Request created - UserId: " + userId + ", Date: " + currentDate);
        Log.d(TAG, "Using API service: " + ApiService.ROUTING.name() + " - " + ApiService.ROUTING.getBaseUrl());

        // Create API service
        RoutingInterface apiService = client.createService(this, ApiService.ROUTING, RoutingInterface.class);

        // Make API call (Authorization header will be added by AuthInterceptor)
        Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call = 
            apiService.getDeliveryRouteByUserAndDate("Bearer " + accessToken, request);

        call.enqueue(new Callback<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse>() {
            @Override
            public void onResponse(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, 
                                 Response<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> response) {
                
                Log.d(TAG, "DeliveryRoute API Response - Code: " + response.code() + ", URL: " + call.request().url());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.skyapp.bo.routing.BO_response.Route> routes = response.body().getRoutes();
                    Log.d(TAG, "Received " + (routes != null ? routes.size() : 0) + " routes");
                    
                    if (routes != null && !routes.isEmpty()) {
                        // Load details for the first route
                        Log.d(TAG, "Loading details for route ID: " + routes.get(0).getRouteId());
                        loadRouteDetails(routes.get(0).getRouteId(), userInfo);
                    } else {
                        Log.d(TAG, "No routes found for today");
                        Log.w(TAG, "No routes returned from API");
                    }
                } else {
                    String errorMsg = "Failed to load routes - Response code: " + response.code();
                    Log.e(TAG, errorMsg);
                    Log.e(TAG, errorMsg);
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, Throwable t) {
                String errorMsg = "Network error loading routes: " + t.getMessage();
                Log.e(TAG, errorMsg);
                Log.e(TAG, "DeliveryRoute API Call failed for URL: " + call.request().url(), t);
            }
        });
    }

    /**
     * Load detailed route information
     */
    private void loadRouteDetails(int routeId, BO_request.UserInfo userInfo) {
        BO_request.RouteDetailsRequest request = new BO_request.RouteDetailsRequest(userInfo, routeId);

        RoutingInterface apiService = client.createService(this, ApiService.ROUTING, RoutingInterface.class);
        Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call = 
            apiService.getDeliveryRouteInfoByRouteId("Bearer " + accessToken, request);

        call.enqueue(new Callback<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse>() {
            @Override
            public void onResponse(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call,
                                 Response<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> response) {
                
                Log.d(TAG, "RouteDetails API Response - Code: " + response.code() + ", URL: " + call.request().url());
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully received route details");
                    processRouteDetails(response.body());
                } else {
                    String errorMsg = "Failed to load route details - Response code: " + response.code();
                    Log.e(TAG, errorMsg);
                    Log.e(TAG, errorMsg);
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Route Details Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading route details error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call, Throwable t) {
                String errorMsg = "Network error loading route details: " + t.getMessage();
                Log.e(TAG, errorMsg);
                Log.e(TAG, "RouteDetails API Call failed for URL: " + call.request().url(), t);
            }
        });
    }

    /**
     * Process route details response and display on map
     */
    private void processRouteDetails(com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse response) {
        try {
            List<com.example.skyapp.bo.routing.BO_response.RouteDetail> routes = response.getData().getRoute();
            
            if (routes != null && !routes.isEmpty()) {
                com.example.skyapp.bo.routing.BO_response.RouteDetail routeDetail = routes.get(0);
                com.example.skyapp.bo.routing.BO_response.MainInfo mainInfo = routeDetail.getData().getMainInfo();
                
                // Clear previous route points
                currentRoutePoints.clear();
                
                // Add initial point as the origin (point 0)
                if (mainInfo.getInitialPoint() != null) {
                    currentRoutePoints.add(mainInfo.getInitialPoint());
                    Log.d(TAG, "Added initial point as origin (0)");
                }
                
                // Add route points and sort them by routePointId to maintain order
                if (mainInfo.getRoutePoints() != null) {
                    List<com.example.skyapp.bo.routing.BO_response.RoutePoint> sortedPoints = 
                        new ArrayList<>(mainInfo.getRoutePoints());
                    
                    // Sort by deliveryOrder to maintain API-optimized delivery sequence
                    sortedPoints.sort((p1, p2) -> {
                        Integer order1 = p1.getDeliveryOrder();
                        Integer order2 = p2.getDeliveryOrder();
                        if (order1 == null) order1 = Integer.MAX_VALUE;
                        if (order2 == null) order2 = Integer.MAX_VALUE;
                        return order1.compareTo(order2);
                    });
                    
                    currentRoutePoints.addAll(sortedPoints);
                    Log.d(TAG, "Added " + sortedPoints.size() + " sorted route points");
                }
                
                // Add end point as the final destination
                if (mainInfo.getEndPoint() != null) {
                    currentRoutePoints.add(mainInfo.getEndPoint());
                    Log.d(TAG, "Added end point as final destination");
                }
                
                Log.d(TAG, "Total route points loaded: " + currentRoutePoints.size());
                
                // Display filtered routes on map
                displayFilteredRoutes();
                
                Log.d(TAG, "Route loaded: " + mainInfo.getHubRoute());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing route details", e);
            Log.e(TAG, "Error processing route data");
        }
    }

    /**
     * Display filtered routes on map based on current filter setting
     */
    private void displayFilteredRoutes() {
        if (mMap == null || currentRoutePoints.isEmpty()) {
            return;
        }

        // Clear existing markers
        clearRouteMarkers();

        // Get points to display based on filter
        List<com.example.skyapp.bo.routing.BO_response.RoutePoint> pointsToShow = getFilteredPoints();
        
        if (pointsToShow.isEmpty()) {
            return;
        }

        // Add markers for filtered points
        addRouteMarkersToMap(pointsToShow);
        
        // Draw route lines between points
        drawRouteLinesOnMap(pointsToShow);
        
        // Move camera to show first point
        com.example.skyapp.bo.routing.BO_response.GeoRef firstPoint = pointsToShow.get(0).getGeoRef();
        if (firstPoint != null) {
            LatLng firstLocation = new LatLng(firstPoint.getLatitude(), firstPoint.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12));
        }
    }

    /**
     * Get filtered points based on current filter setting
     */
    private List<com.example.skyapp.bo.routing.BO_response.RoutePoint> getFilteredPoints() {
        List<com.example.skyapp.bo.routing.BO_response.RoutePoint> filteredPoints = new ArrayList<>();
        
        int pointsToTake = Math.min(currentFilterPoints + 1, currentRoutePoints.size()); // +1 to include starting point
        
        for (int i = 0; i < pointsToTake; i++) {
            filteredPoints.add(currentRoutePoints.get(i));
        }
        
        return filteredPoints;
    }

    /**
     * Clear all route markers from map
     */
    private void clearRouteMarkers() {
        for (Marker marker : routeMarkers) {
            marker.remove();
        }
        routeMarkers.clear();
        mMap.clear(); // Also clears polylines
    }

    /**
     * Add route markers to map
     */
    private void addRouteMarkersToMap(List<com.example.skyapp.bo.routing.BO_response.RoutePoint> points) {
        for (int i = 0; i < points.size(); i++) {
            com.example.skyapp.bo.routing.BO_response.RoutePoint point = points.get(i);
            com.example.skyapp.bo.routing.BO_response.GeoRef geoRef = point.getGeoRef();
            
            if (geoRef != null) {
                LatLng position = new LatLng(geoRef.getLatitude(), geoRef.getLongitude());
                
                // Determine point number for display
                String pointNumber;
                String pointTitle;
                
                if (i == 0) {
                    // Origin point
                    pointNumber = "0";
                    pointTitle = "Origin - " + (point.getConsignee() != null ? point.getConsignee().getName() : "Starting Point");
                } else {
                    // Use deliveryOrder if available, otherwise use sequence number
                    Integer deliveryOrder = point.getDeliveryOrder();
                    if (deliveryOrder != null) {
                        pointNumber = deliveryOrder.toString();
                    } else {
                        pointNumber = String.valueOf(i);
                    }
                    pointTitle = "Point " + pointNumber + " - " + (point.getConsignee() != null ? point.getConsignee().getName() : "Delivery Point");
                }
                
                // Create marker options with point number in title
                MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(pointTitle);
                
                // Set different colors/icons for different points
                if (i == 0) {
                    // Starting point - Green
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (i == points.size() - 1 && points.size() > 1) {
                    // End point - Red  
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else {
                    // Delivery points - Blue
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
                
                // Add detailed info about packages at this point
                StringBuilder infoBuilder = new StringBuilder();
                infoBuilder.append("Point ").append(pointNumber).append("\n");
                
                if (point.getRoutePointData() != null && !point.getRoutePointData().isEmpty()) {
                    infoBuilder.append("Packages (").append(point.getRoutePointData().size()).append("):\n");
                    for (com.example.skyapp.bo.routing.BO_response.RoutePointData packageData : point.getRoutePointData()) {
                        infoBuilder.append("• ").append(packageData.getTrackingNumber())
                                   .append(" (").append(packageData.getDeliveryStatusDescription()).append(")\n");
                    }
                } else {
                    infoBuilder.append("No packages at this point");
                }
                
                markerOptions.snippet(infoBuilder.toString());
                
                Marker marker = mMap.addMarker(markerOptions);
                routeMarkers.add(marker);
                
                Log.d(TAG, "Added marker " + pointNumber + " at: " + position);
            }
        }
    }

    /**
     * Draw route lines on map between points
     */
    private void drawRouteLinesOnMap(List<com.example.skyapp.bo.routing.BO_response.RoutePoint> points) {
        if (points.size() < 2) {
            return;
        }

        // Create a dashed pattern: 20px dash, 10px gap
        List<com.google.android.gms.maps.model.PatternItem> pattern = Arrays.asList(
            new com.google.android.gms.maps.model.Dash(20), 
            new com.google.android.gms.maps.model.Gap(10)
        );

        PolylineOptions polylineOptions = new PolylineOptions()
            .width(6)
            .color(Color.parseColor("#2196F3")) // Material Blue
            .geodesic(true)
            .pattern(pattern); // Apply dashed pattern

        for (com.example.skyapp.bo.routing.BO_response.RoutePoint point : points) {
            com.example.skyapp.bo.routing.BO_response.GeoRef geoRef = point.getGeoRef();
            if (geoRef != null) {
                LatLng position = new LatLng(geoRef.getLatitude(), geoRef.getLongitude());
                polylineOptions.add(position);
                Log.d(TAG, "Added route line point: " + position);
            }
        }

        mMap.addPolyline(polylineOptions);
        Log.d(TAG, "Drew dashed route line connecting " + points.size() + " points");
    }

    /**
     * Show navigation app selection dialog
     */
    private void showNavigationMenu() {
        if (currentRouteId == null) {
            Log.d(TAG, "No route ID available");
            return;
        }
        
        // Get next pending delivery from Realm
        DeliveryPackageRealm nextPackage = realmManager.getNextPendingDelivery(currentRouteId);
        
        if (nextPackage != null) {
            Log.d(TAG, "Found next delivery: " + nextPackage.getConsigneeName() + " at " + nextPackage.getConsigneeAddress());
            showNavigationAppSelectionDialog(nextPackage);
        } else {
            Log.d(TAG, "No pending deliveries found");
            // Try to get next delivery from current route points
            getNextDeliveryFromRoutePointsAndShowDialog();
        }
    }
    
    /**
     * Show elegant dialog to select navigation app with enhanced animations
     */
    private void showNavigationAppSelectionDialog(DeliveryPackageRealm nextPackage) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_navigation_selection, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Add ripple effect and scale animation for cards
        setupCardAnimation(dialogView.findViewById(R.id.cardGoogleMaps));
        setupCardAnimation(dialogView.findViewById(R.id.cardWaze));
        setupCardAnimation(dialogView.findViewById(R.id.cardHereWeGo));

        // Set up click listeners for each option with feedback animation
        dialogView.findViewById(R.id.cardGoogleMaps).setOnClickListener(v -> {
            animateCardClick(v, () -> {
                dialog.dismiss();
                getCurrentLocationAndNavigate(nextPackage, "GoogleMaps");
            });
        });

        dialogView.findViewById(R.id.cardWaze).setOnClickListener(v -> {
            animateCardClick(v, () -> {
                dialog.dismiss();
                getCurrentLocationAndNavigate(nextPackage, "Waze");
            });
        });

        dialogView.findViewById(R.id.cardHereWeGo).setOnClickListener(v -> {
            animateCardClick(v, () -> {
                dialog.dismiss();
                getCurrentLocationAndNavigate(nextPackage, "HereWeGo");
            });
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            animateDialogExit(dialogView, dialog);
        });

        dialog.show();

        // Add entrance animation with staggered effect
        animateDialogEntrance(dialogView);
    }
    
    /**
     * Show elegant dialog for route points when no Realm data available with enhanced animations
     */
    private void showNavigationAppSelectionDialogForRoutePoint(com.example.skyapp.bo.routing.BO_response.RoutePoint routePoint) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_navigation_selection, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Add ripple effect and scale animation for cards
        setupCardAnimation(dialogView.findViewById(R.id.cardGoogleMaps));
        setupCardAnimation(dialogView.findViewById(R.id.cardWaze));
        setupCardAnimation(dialogView.findViewById(R.id.cardHereWeGo));

        // Set up click listeners for each option with feedback animation
        dialogView.findViewById(R.id.cardGoogleMaps).setOnClickListener(v -> {
            animateCardClick(v, () -> {
                dialog.dismiss();
                getCurrentLocationAndNavigateToRoutePoint(routePoint, "GoogleMaps");
            });
        });

        dialogView.findViewById(R.id.cardWaze).setOnClickListener(v -> {
            animateCardClick(v, () -> {
                dialog.dismiss();
                getCurrentLocationAndNavigateToRoutePoint(routePoint, "Waze");
            });
        });

        dialogView.findViewById(R.id.cardHereWeGo).setOnClickListener(v -> {
            animateCardClick(v, () -> {
                dialog.dismiss();
                getCurrentLocationAndNavigateToRoutePoint(routePoint, "HereWeGo");
            });
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            animateDialogExit(dialogView, dialog);
        });

        dialog.show();

        // Add entrance animation with staggered effect
        animateDialogEntrance(dialogView);
    }

    /**
     * Get current location and navigate using selected app
     */
    private void getCurrentLocationAndNavigate(DeliveryPackageRealm nextPackage, String appType) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Set navigation state
        navigationInProgress = true;
        currentNavigationApp = appType;
        currentDestinationName = nextPackage.getConsigneeName();
        currentDestinationAddress = nextPackage.getConsigneeAddress();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    double destLat = nextPackage.getLatitude();
                    double destLng = nextPackage.getLongitude();
                    String address = nextPackage.getConsigneeAddress();
                    
                    if (location != null) {
                        // Use current location as origin
                        double originLat = location.getLatitude();
                        double originLng = location.getLongitude();
                        navigateWithApp(appType, originLat, originLng, destLat, destLng, address);
                        Log.d(TAG, "Navigating from current location to: " + address);
                    } else {
                        Log.w(TAG, "Could not get current location, using destination only");
                        // Navigate to destination without origin
                        navigateWithApp(appType, 0, 0, destLat, destLng, address);
                    }
                });
    }
    
    /**
     * Navigate to route point using selected app
     */
    private void getCurrentLocationAndNavigateToRoutePoint(com.example.skyapp.bo.routing.BO_response.RoutePoint routePoint, String appType) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Set navigation state
        navigationInProgress = true;
        currentNavigationApp = appType;
        currentDestinationName = routePoint.getConsignee().getName();
        currentDestinationAddress = routePoint.getConsignee().getAddress();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    double destLat = routePoint.getGeoRef().getLatitude();
                    double destLng = routePoint.getGeoRef().getLongitude();
                    String address = routePoint.getConsignee().getAddress();
                    
                    if (location != null) {
                        // Use current location as origin
                        double originLat = location.getLatitude();
                        double originLng = location.getLongitude();
                        navigateWithApp(appType, originLat, originLng, destLat, destLng, address);
                        Log.d(TAG, "Navigating from current location to: " + address);
                    } else {
                        Log.w(TAG, "Could not get current location, using destination only");
                        // Navigate to destination without origin
                        navigateWithApp(appType, 0, 0, destLat, destLng, address);
                    }
                });
    }
    
    /**
     * Launch navigation app with coordinates
     */
    private void navigateWithApp(String appType, double originLat, double originLng, double destLat, double destLng, String address) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            
            switch (appType) {
                case "GoogleMaps":
                    if (originLat != 0 && originLng != 0) {
                        // With origin coordinates
                        intent.setData(android.net.Uri.parse(String.format(
                            "https://www.google.com/maps/dir/%f,%f/%f,%f",
                            originLat, originLng, destLat, destLng
                        )));
                    } else {
                        // Destination only
                        intent.setData(android.net.Uri.parse("geo:" + destLat + "," + destLng + "?q=" + android.net.Uri.encode(address)));
                    }
                    intent.setPackage("com.google.android.apps.maps");
                    break;
                    
                case "Waze":
                    if (originLat != 0 && originLng != 0) {
                        // Waze doesn't support origin in URL, so just use destination
                        intent.setData(android.net.Uri.parse("waze://?ll=" + destLat + "," + destLng + "&navigate=yes"));
                    } else {
                        intent.setData(android.net.Uri.parse("waze://?q=" + android.net.Uri.encode(address)));
                    }
                    intent.setPackage("com.waze");
                    break;
                    
                case "HereWeGo":
                    if (originLat != 0 && originLng != 0) {
                        // HERE Maps with coordinates
                        intent.setData(android.net.Uri.parse(String.format(
                            "here-route://%f,%f/%f,%f",
                            originLat, originLng, destLat, destLng
                        )));
                    } else {
                        intent.setData(android.net.Uri.parse("here-location://" + destLat + "," + destLng));
                    }
                    intent.setPackage("com.here.app.maps");
                    break;
            }
            
            startActivity(intent);
            Log.d(TAG, "Launched " + appType + " for navigation");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to open " + appType + ", using fallback", e);
            // Fallback to web version
            navigateWithWebFallback(appType, destLat, destLng, address);
        }
    }
    
    /**
     * Fallback to web version of navigation apps
     */
    private void navigateWithWebFallback(String appType, double destLat, double destLng, String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        
        switch (appType) {
            case "GoogleMaps":
                intent.setData(android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(address)));
                break;
            case "Waze":
                intent.setData(android.net.Uri.parse("https://waze.com/ul?ll=" + destLat + "," + destLng));
                break;
            case "HereWeGo":
                intent.setData(android.net.Uri.parse("https://wego.here.com/directions/mylocation/" + destLat + "," + destLng));
                break;
        }
        
        try {
            startActivity(intent);
            Log.d(TAG, "Opened " + appType + " web version");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open web version", e);
            Toast.makeText(this, "Cannot open navigation app", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get next delivery from route points if Realm doesn't have data and show dialog
     */
    private void getNextDeliveryFromRoutePointsAndShowDialog() {
        if (currentRoutePoints.isEmpty()) {
            Log.d(TAG, "No route points available - load routes first");
            return;
        }

        // Find first route point with packages in pending status
        for (com.example.skyapp.bo.routing.BO_response.RoutePoint point : currentRoutePoints) {
            if (point.getRoutePointData() != null && !point.getRoutePointData().isEmpty()) {
                // Check if any package is in pending status (0)
                for (com.example.skyapp.bo.routing.BO_response.RoutePointData packageData : point.getRoutePointData()) {
                    if (packageData.getDeliveryStatus() == 0) { // Pending
                        // Found a pending package, show navigation app selection
                        if (point.getGeoRef() != null && point.getConsignee() != null) {
                            showNavigationAppSelectionDialogForRoutePoint(point);
                            Log.d(TAG, "Found next pending delivery: " + point.getConsignee().getName());
                            return;
                        }
                    }
                }
            }
        }
        
        // No pending deliveries found
        Log.d(TAG, "All deliveries completed or no deliveries available");
        Toast.makeText(this, "No pending deliveries found", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show elegant arrival confirmation dialog when returning from navigation
     */
    private void showArrivalConfirmationDialog() {
        navigationInProgress = false; // Reset the flag

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_arrival_confirmation, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        // Set destination name
        android.widget.TextView tvDestinationName = dialogView.findViewById(R.id.tvDestinationName);
        tvDestinationName.setText(currentDestinationName);

        // Add pulse animation to the background
        View pulseBackground = dialogView.findViewById(R.id.pulseBackground);
        android.animation.ObjectAnimator pulseAnimator = android.animation.ObjectAnimator.ofFloat(pulseBackground, "scaleX", 1.0f, 1.2f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        pulseAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        pulseAnimator.start();

        android.animation.ObjectAnimator pulseAnimatorY = android.animation.ObjectAnimator.ofFloat(pulseBackground, "scaleY", 1.0f, 1.2f);
        pulseAnimatorY.setDuration(1000);
        pulseAnimatorY.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        pulseAnimatorY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        pulseAnimatorY.start();

        // Set up click listeners
        dialogView.findViewById(R.id.btnArrived).setOnClickListener(v -> {
            dialog.dismiss();
            // Redirect to Scan activity
            Intent intent = new Intent(MapsActivity.this, CheckpointActivity.class);
            startActivity(intent);
        });

        dialogView.findViewById(R.id.btnNavigateAgain).setOnClickListener(v -> {
            dialog.dismiss();
            // Navigate again with the same app
            restartNavigation();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            // Stay in current activity - just dismiss dialog
            dialog.dismiss();
        });

        dialog.show();

        // Add entrance animation
        dialogView.setAlpha(0f);
        dialogView.setTranslationY(50f);
        dialogView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }
    
    /**
     * Restart navigation with the same app and destination
     */
    private void restartNavigation() {
        navigationInProgress = true;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            
            switch (currentNavigationApp) {
                case "GoogleMaps":
                    intent.setData(android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(currentDestinationAddress)));
                    intent.setPackage("com.google.android.apps.maps");
                    break;
                case "Waze":
                    intent.setData(android.net.Uri.parse("waze://?q=" + android.net.Uri.encode(currentDestinationAddress)));
                    intent.setPackage("com.waze");
                    break;
                case "HereWeGo":
                    intent.setData(android.net.Uri.parse("here-location://" + android.net.Uri.encode(currentDestinationAddress)));
                    intent.setPackage("com.here.app.maps");
                    break;
            }
            
            startActivity(intent);
            Log.d(TAG, "Restarted navigation with " + currentNavigationApp);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to restart navigation", e);
            // Fallback to web version
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(currentDestinationAddress)));
            try {
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "Cannot restart navigation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is returning from navigation
        if (navigationInProgress) {
            showArrivalConfirmationDialog();
        }
    }
    
    /**
     * Setup card animation effects (hover, scale on touch)
     */
    private void setupCardAnimation(View card) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                    break;
            }
            return false;
        });
    }

    /**
     * Animate card click with feedback before executing action
     */
    private void animateCardClick(View card, Runnable action) {
        card.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(() -> {
                    card.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .withEndAction(action)
                            .start();
                })
                .start();
    }

    /**
     * Animate dialog entrance with staggered card animations
     */
    private void animateDialogEntrance(View dialogView) {
        // Initial state - dialog slides up and fades in
        dialogView.setAlpha(0f);
        dialogView.setTranslationY(100f);
        dialogView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        // Staggered card animations
        View[] cards = {
                dialogView.findViewById(R.id.cardGoogleMaps),
                dialogView.findViewById(R.id.cardWaze),
                dialogView.findViewById(R.id.cardHereWeGo)
        };

        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            card.setAlpha(0f);
            card.setTranslationX(-50f);
            card.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setStartDelay(100 + (i * 100)) // Staggered delay
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }

        // Cancel button animation
        View cancelBtn = dialogView.findViewById(R.id.btnCancel);
        cancelBtn.setAlpha(0f);
        cancelBtn.animate()
                .alpha(1f)
                .setStartDelay(500)
                .setDuration(200)
                .start();
    }

    /**
     * Animate dialog exit
     */
    private void animateDialogExit(View dialogView, android.app.AlertDialog dialog) {
        dialogView.animate()
                .alpha(0f)
                .translationY(50f)
                .setDuration(200)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(dialog::dismiss)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realmManager != null) {
            realmManager.close();
        }
    }

}
