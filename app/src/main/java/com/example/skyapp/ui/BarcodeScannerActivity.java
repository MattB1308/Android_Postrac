package com.example.skyapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.example.skyapp.R;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {
    
    private static final String TAG = "BarcodeScannerActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    
    private PreviewView previewView;
    private TextView instructionText;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private boolean isScanning = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        
        Log.d(TAG, "BarcodeScannerActivity created");
        
        initializeViews();
        setupActionBar();
        initializeBarcodeScanner();
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        instructionText = findViewById(R.id.instructionText);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan Barcode");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void initializeBarcodeScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_DATA_MATRIX,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_ITF,
                        Barcode.FORMAT_PDF417,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E)
                .build();
        
        barcodeScanner = BarcodeScanning.getClient(options);
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                REQUEST_CAMERA_PERMISSION);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required for barcode scanning", 
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Build the preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        // Build the image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        
        imageAnalysis.setAnalyzer(cameraExecutor, new BarcodeAnalyzer());
        
        // Select back camera as default
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll();
            
            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);
                    
            Log.d(TAG, "Camera started successfully");
            updateInstructionText("Position barcode within the frame");
            
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
        }
    }
    
    private class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        @androidx.camera.core.ExperimentalGetImage
        public void analyze(@NonNull ImageProxy imageProxy) {
            if (!isScanning) {
                imageProxy.close();
                return;
            }
            
            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(
                        mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                
                Task<java.util.List<Barcode>> result = barcodeScanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                if (rawValue != null && !rawValue.isEmpty()) {
                                    isScanning = false;
                                    returnResult(rawValue);
                                    break;
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Barcode scanning failed", e);
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        }
    }
    
    private void returnResult(String barcode) {
        runOnUiThread(() -> {
            Log.d(TAG, "Barcode found: " + barcode);
            updateInstructionText("✓ Barcode found: " + barcode);
            
            // Provide haptic feedback if available
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(100);
                }
            } catch (Exception e) {
                Log.w(TAG, "Vibration not available");
            }
            
            // Show success toast
            Toast.makeText(this, "Barcode scanned successfully!", Toast.LENGTH_SHORT).show();
            
            // Small delay to show the result before finishing
            new android.os.Handler().postDelayed(() -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("barcode", barcode);
                setResult(RESULT_OK, resultIntent);
                finish();
            }, 1500);
        });
    }
    
    private void updateInstructionText(String text) {
        runOnUiThread(() -> {
            if (instructionText != null) {
                instructionText.setText(text);
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }
}