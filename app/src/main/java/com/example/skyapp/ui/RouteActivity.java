package com.example.skyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skyapp.R;
import com.google.android.material.button.MaterialButton;

public class RouteActivity extends AppCompatActivity {

    private MaterialButton btnViewAllRoutes;
    private MaterialButton btnPlanRoute;
    private MaterialButton btnOptimizeRoutes;
    private MaterialButton btnBackToMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnViewAllRoutes = findViewById(R.id.btnViewAllRoutes);
        btnPlanRoute = findViewById(R.id.btnPlanRoute);
        btnOptimizeRoutes = findViewById(R.id.btnOptimizeRoutes);
        btnBackToMaps = findViewById(R.id.btnBackToMaps);
    }

    private void setupClickListeners() {
        // Dummy functionality for action buttons
        btnViewAllRoutes.setOnClickListener(v -> {
            Toast.makeText(this, "View All Routes - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnPlanRoute.setOnClickListener(v -> {
            Toast.makeText(this, "Plan New Route - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnOptimizeRoutes.setOnClickListener(v -> {
            Toast.makeText(this, "Optimize Routes - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Back to Maps functionality
        btnBackToMaps.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}