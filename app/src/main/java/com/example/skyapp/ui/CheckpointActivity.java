package com.example.skyapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.card.MaterialCardView;
import com.example.skyapp.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckpointActivity extends AppCompatActivity {

    private static final String TAG = "CheckpointActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_BARCODE_SCAN = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    
    // UI Components
    private TextView barcodeText;
    private Button scanBarcodeButton;
    private Button takePhotoButton;
    private TextView photoCountText;
    private GridLayout photoGrid;
    private Button deliveredButton;
    private Button exceptionButton;
    private TextView resetButton;
    private MaterialCardView statusCard;
    private TextView statusText;
    private ImageView statusIcon;
    
    // New status message components
    private LinearLayout statusMessageCard;
    private TextView statusMessageText;
    private ImageView statusMessageIcon;
    
    // Data
    private String scannedBarcode = "";
    private List<String> capturedImagePaths = new ArrayList<>();
    private String currentPhotoPath;
    
    // Status enum
    private enum DeliveryStatus {
        DELIVERED, EXCEPTION, NONE
    }
    
    private DeliveryStatus currentStatus = DeliveryStatus.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pod_scanner);
        
        initializeViews();
        setupClickListeners();
        checkCameraPermission();
        updateUI();
    }

    private void initializeViews() {
        barcodeText = findViewById(R.id.barcodeText);
        scanBarcodeButton = findViewById(R.id.scanBarcodeButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        photoCountText = findViewById(R.id.photoCountText);
        photoGrid = findViewById(R.id.photoGrid);
        deliveredButton = findViewById(R.id.deliveredButton);
        exceptionButton = findViewById(R.id.exceptionButton);
        resetButton = findViewById(R.id.resetButton);
        statusCard = findViewById(R.id.statusCard);
        statusText = findViewById(R.id.statusText);
        statusIcon = findViewById(R.id.statusIcon);
        
        // New status message components
        statusMessageCard = findViewById(R.id.statusMessageCard);
        statusMessageText = findViewById(R.id.statusMessageText);
        statusMessageIcon = findViewById(R.id.statusMessageIcon);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan");
        }
    }

    private void setupClickListeners() {
        // Setup navigation first
        NavigationHelper.setupNavigation(this, CheckpointActivity.class);
        NavigationHelper.highlightCurrentSection(this, CheckpointActivity.class);
        
        // Then setup pod scanner click listeners (these should override any conflicts)
        scanBarcodeButton.setOnClickListener(v -> {
            Log.d(TAG, "Scan button clicked - starting barcode scanner");
            startBarcodeScanner();
        });
        
        takePhotoButton.setOnClickListener(v -> {
            Log.d(TAG, "Take Photo button clicked");
            if (capturedImagePaths.size() < 4) {
                takePhoto();
            } else {
                Toast.makeText(this, "Maximum 4 photos allowed", Toast.LENGTH_SHORT).show();
            }
        });
        
        deliveredButton.setOnClickListener(v -> {
            Log.d(TAG, "Delivered button clicked");
            markAsDelivered();
        });
        
        exceptionButton.setOnClickListener(v -> {
            Log.d(TAG, "Exception button clicked");
            markAsException();
        });
        
        resetButton.setOnClickListener(v -> {
            Log.d(TAG, "Reset button clicked");
            resetForm();
        });
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    REQUEST_CAMERA_PERMISSION);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startBarcodeScanner() {
        Log.d(TAG, "startBarcodeScanner() called");
        try {
            Intent intent = new Intent(this, BarcodeScannerActivity.class);
            Log.d(TAG, "Intent created, starting activity");
            startActivityForResult(intent, REQUEST_BARCODE_SCAN);
        } catch (Exception e) {
            Log.e(TAG, "Error starting barcode scanner", e);
            // Fallback: simulate a scanned barcode after a short delay
            Toast.makeText(this, "Simulating barcode scan...", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(() -> {
                scannedBarcode = "DEMO" + System.currentTimeMillis() % 10000;
                updateUI();
                Toast.makeText(this, "Barcode scanned: " + scannedBarcode, Toast.LENGTH_SHORT).show();
            }, 2000);
        }
    }
    
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error occurred while creating the File", ex);
                return;
            }
            
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.skyapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "POD_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_BARCODE_SCAN && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("barcode")) {
                scannedBarcode = data.getStringExtra("barcode");
                updateUI();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (currentPhotoPath != null) {
                capturedImagePaths.add(currentPhotoPath);
                updateUI();
                addPhotoToGrid(currentPhotoPath);
            }
        }
    }
    
    private void addPhotoToGrid(String imagePath) {
        View photoView = getLayoutInflater().inflate(R.layout.photo_item, photoGrid, false);
        ImageView imageView = photoView.findViewById(R.id.photoImageView);
        MaterialCardView deleteButton = photoView.findViewById(R.id.deleteButton);
        
        // Load and display the image
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        
        // Set delete button click listener
        deleteButton.setOnClickListener(v -> {
            showDeletePhotoDialog(imagePath, photoView);
        });
        
        photoGrid.addView(photoView);
        Log.d(TAG, "Photo added to grid: " + imagePath);
    }
    
    private void updateUI() {
        updateUIWithoutStatus();
        // Update status display
        updateStatusDisplay();
    }
    
    private void updateUIWithoutStatus() {
        // Update barcode display
        if (scannedBarcode.isEmpty()) {
            barcodeText.setText("No barcode scanned");
            barcodeText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            barcodeText.setText(scannedBarcode);
            barcodeText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
        
        // Update photo count
        photoCountText.setText(String.format(Locale.getDefault(), 
                "Photos (%d/4)", capturedImagePaths.size()));
        
        // Update Take Photo button state
        takePhotoButton.setEnabled(capturedImagePaths.size() < 4);
        takePhotoButton.setAlpha(capturedImagePaths.size() < 4 ? 1.0f : 0.5f);
        
        // Update action buttons
        boolean canSubmit = !scannedBarcode.isEmpty() && !capturedImagePaths.isEmpty();
        deliveredButton.setEnabled(canSubmit);
        exceptionButton.setEnabled(canSubmit);
    }
    
    private void updateStatusDisplay() {
        switch (currentStatus) {
            case DELIVERED:
                statusCard.setVisibility(View.VISIBLE);
                statusText.setText("Marked as Delivered");
                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                statusIcon.setImageResource(R.drawable.ic_check_circle);
                statusIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                statusCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
                break;
            case EXCEPTION:
                statusCard.setVisibility(View.VISIBLE);
                statusText.setText("Marked as Exception");
                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                statusIcon.setImageResource(R.drawable.ic_warning);
                statusIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                statusCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_orange));
                break;
            case NONE:
            default:
                statusCard.setVisibility(View.GONE);
                break;
        }
    }
    
    private void markAsDelivered() {
        currentStatus = DeliveryStatus.DELIVERED;
        
        // Show status message
        showStatusMessage("Marked as Delivered", R.drawable.ic_check_circle, R.color.sepex_green);
        
        // Process the delivery
        processDelivery(true);
        
        // Update UI without showing status display (to avoid duplicate)
        updateUIWithoutStatus();
        Log.d(TAG, "Package marked as delivered");
    }
    
    private void markAsException() {
        currentStatus = DeliveryStatus.EXCEPTION;
        
        // Show status message
        showStatusMessage("Marked as Exception", R.drawable.ic_warning, android.R.color.holo_orange_dark);
        
        // Process the exception
        processDelivery(false);
        
        // Update UI without showing status display (to avoid duplicate)
        updateUIWithoutStatus();
        Log.d(TAG, "Package marked as exception");
    }
    
    private void showStatusMessage(String message, int iconRes, int colorRes) {
        if (statusMessageCard != null && statusMessageText != null && statusMessageIcon != null) {
            statusMessageText.setText(message);
            statusMessageIcon.setImageResource(iconRes);
            
            // Set colors based on status type
            if (colorRes == R.color.sepex_green || colorRes == android.R.color.holo_green_dark) {
                // Delivered - Green
                statusMessageIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                statusMessageText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                statusMessageCard.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
            } else {
                // Exception - Orange
                statusMessageIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                statusMessageText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                statusMessageCard.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange));
            }
            
            statusMessageCard.setVisibility(View.VISIBLE);
            Log.d(TAG, "Status message shown: " + message);
        }
    }
    
    private void processDelivery(boolean delivered) {
        // Here you would typically send the data to your backend
        Log.d(TAG, String.format("Processing delivery - Status: %s, Barcode: %s, Photos: %d",
                delivered ? "Delivered" : "Exception", scannedBarcode, capturedImagePaths.size()));
        
        // Example: Send to API
        // ApiManager.getInstance().submitPodData(scannedBarcode, capturedImagePaths, delivered);
    }
    
    private void resetForm() {
        showResetFormDialog();
    }

    /**
     * Show elegant reset form confirmation dialog
     */
    private void showResetFormDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reset_form, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Update data summary
        TextView tvBarcodeStatus = dialogView.findViewById(R.id.tvBarcodeStatus);
        TextView tvPhotoCount = dialogView.findViewById(R.id.tvPhotoCount);

        tvBarcodeStatus.setText(scannedBarcode.isEmpty() ? "None" : "Scanned");
        tvPhotoCount.setText(String.valueOf(capturedImagePaths.size()));

        // Add rotation animation to reset background
        View resetRotateBackground = dialogView.findViewById(R.id.resetRotateBackground);
        android.animation.ObjectAnimator rotateAnimator = android.animation.ObjectAnimator.ofFloat(resetRotateBackground, "rotation", 0f, 360f);
        rotateAnimator.setDuration(3000);
        rotateAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        rotateAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        rotateAnimator.start();

        // Set up click listeners
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            // Add exit animation
            dialogView.animate()
                    .alpha(0f)
                    .translationY(30f)
                    .setDuration(150)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(dialog::dismiss)
                    .start();
        });

        dialogView.findViewById(R.id.btnReset).setOnClickListener(v -> {
            // Add reset animation effect
            dialogView.findViewById(R.id.btnReset).animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        // Perform reset
                        performReset();

                        // Close dialog with animation
                        dialogView.animate()
                                .alpha(0f)
                                .translationY(30f)
                                .setDuration(150)
                                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                                .withEndAction(dialog::dismiss)
                                .start();
                    })
                    .start();
        });

        dialog.show();

        // Add entrance animation
        dialogView.setAlpha(0f);
        dialogView.setTranslationY(50f);
        dialogView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    /**
     * Perform the actual reset operation
     */
    private void performReset() {
        // Clear barcode
        scannedBarcode = "";

        // Clear images
        for (String imagePath : capturedImagePaths) {
            File file = new File(imagePath);
            if (file.exists()) {
                file.delete();
            }
        }
        capturedImagePaths.clear();

        // Clear photo grid
        photoGrid.removeAllViews();

        // Reset status
        currentStatus = DeliveryStatus.NONE;

        // Hide status message
        if (statusMessageCard != null) {
            statusMessageCard.setVisibility(View.GONE);
        }

        // Update UI
        updateUI();

        Toast.makeText(this, "Form reset successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * Show elegant delete photo confirmation dialog
     */
    private void showDeletePhotoDialog(String imagePath, View photoView) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_photo, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Add pulse animation to warning background
        View warningPulseBackground = dialogView.findViewById(R.id.warningPulseBackground);
        android.animation.ObjectAnimator pulseAnimator = android.animation.ObjectAnimator.ofFloat(warningPulseBackground, "scaleX", 1.0f, 1.15f);
        pulseAnimator.setDuration(800);
        pulseAnimator.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        pulseAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        pulseAnimator.start();

        android.animation.ObjectAnimator pulseAnimatorY = android.animation.ObjectAnimator.ofFloat(warningPulseBackground, "scaleY", 1.0f, 1.15f);
        pulseAnimatorY.setDuration(800);
        pulseAnimatorY.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        pulseAnimatorY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        pulseAnimatorY.start();

        // Set up click listeners
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            // Add exit animation
            dialogView.animate()
                    .alpha(0f)
                    .translationY(30f)
                    .setDuration(150)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(dialog::dismiss)
                    .start();
        });

        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            // Add delete animation effect
            dialogView.findViewById(R.id.btnDelete).animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        // Perform deletion
                        capturedImagePaths.remove(imagePath);
                        photoGrid.removeView(photoView);

                        // Delete the file
                        File file = new File(imagePath);
                        if (file.exists()) {
                            file.delete();
                        }

                        updateUI();
                        Log.d(TAG, "Photo deleted: " + imagePath);

                        // Close dialog with animation
                        dialogView.animate()
                                .alpha(0f)
                                .translationY(30f)
                                .setDuration(150)
                                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                                .withEndAction(dialog::dismiss)
                                .start();
                    })
                    .start();
        });

        dialog.show();

        // Add entrance animation
        dialogView.setAlpha(0f);
        dialogView.setTranslationY(50f);
        dialogView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }
}