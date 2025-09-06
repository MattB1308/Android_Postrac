package com.example.skyapp.ui;



import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.skyapp.R;
import com.example.skyapp.bo.BO_response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.skyapp.databinding.ActivityMapsBinding;
import com.google.gson.Gson;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Recuperar el JSON de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
        String loginResponseJson = sharedPreferences.getString("login_response", null);

        // Verificar si se guardó un valor en shared preferences
        if (loginResponseJson != null) {
            // Deserializar el JSON a un objeto LoginResponse
            BO_response.LoginResponse loginResponse = new Gson().fromJson(loginResponseJson, BO_response.LoginResponse.class);

            // Verificar si se recuperó correctamente el objeto
            if (loginResponse != null) {
                String accessToken = loginResponse.getAccessToken();
                String refreshToken = loginResponse.getRefreshToken();
                Integer userID = loginResponse.getUserId();
                String userUuid = loginResponse.getUserUuid();

                Log.d("SharedPreferences", "Success access token: " + accessToken);
                Log.d("SharedPreferences", "Success refresh token: " + refreshToken);
                Log.d("SharedPreferences", "Success userID: " + userID);
                Log.d("SharedPreferences", "Success userUuid: " + userUuid);

            } else {
                Toast.makeText(this, "No valid response data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No login data found", Toast.LENGTH_SHORT).show();
        }


        // Inicializar el FusedLocationProviderClient para obtener la ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


}
