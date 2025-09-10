package com.example.skyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skyapp.R;
import com.google.android.material.button.MaterialButton;

public class ShipmentsActivity extends AppCompatActivity {

    private MaterialButton btnTrackShipments;
    private MaterialButton btnCreateShipment;
    private MaterialButton btnViewReports;
    private MaterialButton btnBackToMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipments);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnTrackShipments = findViewById(R.id.btnTrackShipments);
        btnCreateShipment = findViewById(R.id.btnCreateShipment);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnBackToMaps = findViewById(R.id.btnBackToMaps);
    }

    private void setupClickListeners() {
        // Dummy functionality for action buttons
        btnTrackShipments.setOnClickListener(v -> {
            Toast.makeText(this, "Track Shipments - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnCreateShipment.setOnClickListener(v -> {
            Toast.makeText(this, "Create New Shipment - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnViewReports.setOnClickListener(v -> {
            Toast.makeText(this, "View Reports - Coming Soon", Toast.LENGTH_SHORT).show();
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