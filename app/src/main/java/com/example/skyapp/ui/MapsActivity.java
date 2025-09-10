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
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private MaterialButton btnShowNext1, btnShowNext5, btnShowNext10;
    private MaterialButton btnLoadRoutes;
    private FloatingActionButton fabFilter;
    private FloatingActionButton fabLocation;
    
    // Data
    private String accessToken;
    private int userId;
    private String userUuid;
    private List<com.example.skyapp.bo.routing.BO_response.RoutePoint> currentRoutePoints;
    private List<Marker> routeMarkers;
    private int currentFilterPoints = 1; // Default: show next 1 point
    private boolean filterCardVisible = false;

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
        btnLoadRoutes = findViewById(R.id.btnLoadRoutes);
        fabFilter = findViewById(R.id.fab_filter);
        fabLocation = findViewById(R.id.fab_location);
    }

    private void setupClickListeners() {
        // Navigation buttons
        LinearLayout btnProfile = findViewById(R.id.btn_profile);
        LinearLayout btnShipment = findViewById(R.id.btn_shipment);
        LinearLayout btnRoute = findViewById(R.id.btn_route);
        LinearLayout btnCheckpoint = findViewById(R.id.btn_checkpoint);

        btnProfile.setOnClickListener(v -> {
            Intent loadingIntent = LoadingActivity.createIntent(
                this,
                getString(R.string.loading_profile_message),
                ProfileActivity.class,
                1500
            );
            startActivity(loadingIntent);
        });

        btnShipment.setOnClickListener(v -> {
            Intent loadingIntent = LoadingActivity.createIntent(
                this,
                getString(R.string.loading_shipments_message),
                ShipmentsActivity.class,
                3000
            );
            startActivity(loadingIntent);
        });

        btnRoute.setOnClickListener(v -> {
            Intent loadingIntent = LoadingActivity.createIntent(
                this,
                getString(R.string.loading_routes_message),
                RouteActivity.class,
                2800
            );
            startActivity(loadingIntent);
        });

        btnCheckpoint.setOnClickListener(v -> {
            Intent loadingIntent = LoadingActivity.createIntent(
                this,
                getString(R.string.loading_checkpoints_message),
                CheckpointActivity.class,
                3200
            );
            startActivity(loadingIntent);
        });

        // FAB listeners
        fabLocation.setOnClickListener(v -> getCurrentLocation());
        fabFilter.setOnClickListener(v -> toggleFilterCard());

        // Filter buttons
        btnShowNext1.setOnClickListener(v -> setFilterPoints(1));
        btnShowNext5.setOnClickListener(v -> setFilterPoints(5));
        btnShowNext10.setOnClickListener(v -> setFilterPoints(10));
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

                    Log.d(TAG, "Login data loaded - UserId: " + userId + ", UserUuid: " + userUuid);
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

        // Obtener la ubicación actual
        getCurrentLocation();
        
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
     * Update filter button visual states
     */
    private void updateFilterButtonStates() {
        // Reset all buttons to outlined style
        resetFilterButtons();
        
        // Set selected button to filled style
        MaterialButton selectedButton;
        switch (currentFilterPoints) {
            case 5:
                selectedButton = btnShowNext5;
                break;
            case 10:
                selectedButton = btnShowNext10;
                break;
            default:
                selectedButton = btnShowNext1;
                break;
        }
        
        // Update selected button appearance
        selectedButton.setBackgroundTintList(getColorStateList(R.color.sepex_blue));
    }

    /**
     * Reset all filter buttons to outlined style
     */
    private void resetFilterButtons() {
        MaterialButton[] buttons = {btnShowNext1, btnShowNext5, btnShowNext10};
        for (MaterialButton button : buttons) {
            button.setBackgroundTintList(null);
        }
    }

    /**
     * Load delivery routes from API
     */
    private void loadDeliveryRoutes() {
        if (accessToken == null || userUuid == null) {
            Toast.makeText(this, "Login data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, "Loading routes...", Toast.LENGTH_SHORT).show();

        // Create request objects
        BO_request.User user = new BO_request.User(userId, userUuid);
        BO_request.Application application = new BO_request.Application("83d6661f-9f64-43c4-b672-cdcab3a57685");
        BO_request.UserInfo userInfo = new BO_request.UserInfo(user, application);

        // Get current date in required format
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
                        Toast.makeText(MapsActivity.this, "No routes found for today", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Failed to load routes", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.DeliveryRouteResponse> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Network error loading routes", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API Call failed", t);
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
                
                if (response.isSuccessful() && response.body() != null) {
                    processRouteDetails(response.body());
                } else {
                    Toast.makeText(MapsActivity.this, "Failed to load route details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Route Details API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.skyapp.bo.routing.BO_response.RouteDetailsResponse> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Network error loading route details", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Route Details API Call failed", t);
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
                
                // Add initial point
                if (mainInfo.getInitialPoint() != null) {
                    currentRoutePoints.add(mainInfo.getInitialPoint());
                }
                
                // Add route points (these are ordered by priority)
                if (mainInfo.getRoutePoints() != null) {
                    currentRoutePoints.addAll(mainInfo.getRoutePoints());
                }
                
                // Add end point
                if (mainInfo.getEndPoint() != null) {
                    currentRoutePoints.add(mainInfo.getEndPoint());
                }
                
                Log.d(TAG, "Loaded " + currentRoutePoints.size() + " route points");
                
                // Display filtered routes on map
                displayFilteredRoutes();
                
                Toast.makeText(this, "Route loaded: " + mainInfo.getHubRoute(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing route details", e);
            Toast.makeText(this, "Error processing route data", Toast.LENGTH_SHORT).show();
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
                
                // Create marker options
                MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(point.getConsignee() != null ? point.getConsignee().getName() : "Delivery Point");
                
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
                
                // Add info about packages at this point
                if (point.getRoutePointData() != null && !point.getRoutePointData().isEmpty()) {
                    StringBuilder infoBuilder = new StringBuilder();
                    infoBuilder.append("Packages: ");
                    for (com.example.skyapp.bo.routing.BO_response.RoutePointData packageData : point.getRoutePointData()) {
                        infoBuilder.append(packageData.getTrackingNumber()).append(" (")
                                   .append(packageData.getDeliveryStatusDescription()).append("), ");
                    }
                    markerOptions.snippet(infoBuilder.toString());
                }
                
                Marker marker = mMap.addMarker(markerOptions);
                routeMarkers.add(marker);
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

        PolylineOptions polylineOptions = new PolylineOptions()
            .width(5)
            .color(Color.BLUE)
            .geodesic(true);

        for (com.example.skyapp.bo.routing.BO_response.RoutePoint point : points) {
            com.example.skyapp.bo.routing.BO_response.GeoRef geoRef = point.getGeoRef();
            if (geoRef != null) {
                polylineOptions.add(new LatLng(geoRef.getLatitude(), geoRef.getLongitude()));
            }
        }

        mMap.addPolyline(polylineOptions);
    }

}
