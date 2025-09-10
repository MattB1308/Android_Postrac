package com.example.skyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skyapp.R;
import com.google.android.material.button.MaterialButton;

public class CheckpointActivity extends AppCompatActivity {

    private MaterialButton btnViewAll;
    private MaterialButton btnCreateNew;
    private MaterialButton btnSearch;
    private MaterialButton btnBackToMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoint);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnViewAll = findViewById(R.id.btnViewAll);
        btnCreateNew = findViewById(R.id.btnCreateNew);
        btnSearch = findViewById(R.id.btnSearch);
        btnBackToMaps = findViewById(R.id.btnBackToMaps);
    }

    private void setupClickListeners() {
        // Dummy functionality for action buttons
        btnViewAll.setOnClickListener(v -> {
            Toast.makeText(this, "View All Checkpoints - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnCreateNew.setOnClickListener(v -> {
            Toast.makeText(this, "Create New Checkpoint - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Search Checkpoints - Coming Soon", Toast.LENGTH_SHORT).show();
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