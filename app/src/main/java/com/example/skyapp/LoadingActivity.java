package com.example.skyapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class LoadingActivity extends AppCompatActivity {

    // Intent extras constants
    public static final String EXTRA_LOADING_MESSAGE = "loading_message";
    public static final String EXTRA_TARGET_ACTIVITY = "target_activity";
    public static final String EXTRA_LOADING_DURATION = "loading_duration";
    public static final String EXTRA_USE_CIRCULAR_PROGRESS = "use_circular_progress";
    public static final String EXTRA_SHOW_PERCENTAGE = "show_percentage";

    // Default values
    private static final int DEFAULT_LOADING_DURATION = 500; // 0.5 seconds
    private static final String DEFAULT_MESSAGE = "Loading...";

    // UI components
    private ImageView ivSepexLogo;
    private TextView tvLoadingMessage;
    private TextView tvProgressPercentage;
    private TextView tvSecondaryMessage;
    private LinearProgressIndicator progressBarLinear;
    private CircularProgressIndicator progressBarCircular;

    // Progress animation
    private ObjectAnimator logoAnimator;
    private Handler progressHandler;
    private Runnable progressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        initializeViews();
        setupFromIntent();
        startLoading();
    }

    private void initializeViews() {
        ivSepexLogo = findViewById(R.id.ivSepexLogo);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        tvSecondaryMessage = findViewById(R.id.tvSecondaryMessage);
        progressBarLinear = findViewById(R.id.progressBarLinear);
        progressBarCircular = findViewById(R.id.progressBarCircular);
    }

    private void setupFromIntent() {
        Intent intent = getIntent();
        
        // Set loading message
        String message = intent.getStringExtra(EXTRA_LOADING_MESSAGE);
        if (message != null) {
            tvLoadingMessage.setText(message);
        }

        // Configure progress bar type
        boolean useCircularProgress = intent.getBooleanExtra(EXTRA_USE_CIRCULAR_PROGRESS, false);
        if (useCircularProgress) {
            progressBarLinear.setVisibility(View.GONE);
            progressBarCircular.setVisibility(View.VISIBLE);
            tvProgressPercentage.setVisibility(View.GONE);
        }

        // Show/hide percentage
        boolean showPercentage = intent.getBooleanExtra(EXTRA_SHOW_PERCENTAGE, true);
        if (!showPercentage || useCircularProgress) {
            tvProgressPercentage.setVisibility(View.GONE);
        }
    }

    private void startLoading() {
        // Start logo animation
        startLogoAnimation();
        
        // Get loading duration
        Intent intent = getIntent();
        int duration = intent.getIntExtra(EXTRA_LOADING_DURATION, DEFAULT_LOADING_DURATION);
        
        // Start progress animation
        if (progressBarCircular.getVisibility() == View.VISIBLE) {
            // Circular progress is already indeterminate, just wait
            navigateAfterDelay(duration);
        } else {
            // Animate linear progress
            animateLinearProgress(duration);
        }
    }

    private void startLogoAnimation() {
        // Subtle breathing animation for the logo
        logoAnimator = ObjectAnimator.ofFloat(ivSepexLogo, "scaleX", 1f, 1.1f, 1f);
        logoAnimator.setDuration(2000);
        logoAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        logoAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        logoAnimator.start();

        // Apply same animation to scaleY
        ObjectAnimator logoAnimatorY = ObjectAnimator.ofFloat(ivSepexLogo, "scaleY", 1f, 1.1f, 1f);
        logoAnimatorY.setDuration(2000);
        logoAnimatorY.setRepeatCount(ObjectAnimator.INFINITE);
        logoAnimatorY.setInterpolator(new AccelerateDecelerateInterpolator());
        logoAnimatorY.start();
    }

    private void animateLinearProgress(int duration) {
        progressHandler = new Handler(Looper.getMainLooper());
        final int updateInterval = 50; // Update every 50ms for smooth animation
        final int totalSteps = duration / updateInterval;
        
        progressRunnable = new Runnable() {
            int currentStep = 0;
            
            @Override
            public void run() {
                if (currentStep <= totalSteps) {
                    int progress = (currentStep * 100) / totalSteps;
                    progressBarLinear.setProgress(progress);
                    
                    if (tvProgressPercentage.getVisibility() == View.VISIBLE) {
                        tvProgressPercentage.setText(progress + "%");
                    }
                    
                    currentStep++;
                    progressHandler.postDelayed(this, updateInterval);
                } else {
                    // Loading complete
                    navigateToNextActivity();
                }
            }
        };
        
        progressHandler.post(progressRunnable);
    }

    private void navigateAfterDelay(int delay) {
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextActivity, delay);
    }

    private void navigateToNextActivity() {
        Intent intent = getIntent();
        String targetActivity = intent.getStringExtra(EXTRA_TARGET_ACTIVITY);
        
        if (targetActivity != null) {
            try {
                Class<?> targetClass = Class.forName(targetActivity);
                Intent nextIntent = new Intent(this, targetClass);
                
                // Note: Not copying original intent extras to avoid type casting issues
                // If needed, specific data can be passed through SharedPreferences or custom methods
                
                startActivity(nextIntent);
                finish();
                
                // Add fade transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                
            } catch (ClassNotFoundException e) {
                // If target activity not found, just finish
                finish();
            }
        } else {
            // No target specified, just finish
            finish();
        }
    }

    private boolean isLoadingExtra(String key) {
        return EXTRA_LOADING_MESSAGE.equals(key) ||
               EXTRA_TARGET_ACTIVITY.equals(key) ||
               EXTRA_LOADING_DURATION.equals(key) ||
               EXTRA_USE_CIRCULAR_PROGRESS.equals(key) ||
               EXTRA_SHOW_PERCENTAGE.equals(key);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up animations and handlers
        if (logoAnimator != null) {
            logoAnimator.cancel();
        }
        
        if (progressHandler != null && progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button during loading to prevent interruption
        // Optionally, you can allow back button by calling super.onBackPressed()
        super.onBackPressed();
    }

    // Helper method to create intent for this activity
    public static Intent createIntent(android.content.Context context, 
                                     String message, 
                                     Class<?> targetActivity, 
                                     int duration) {
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.putExtra(EXTRA_LOADING_MESSAGE, message);
        intent.putExtra(EXTRA_TARGET_ACTIVITY, targetActivity.getName());
        intent.putExtra(EXTRA_LOADING_DURATION, duration);
        return intent;
    }

    // Helper method with default duration
    public static Intent createIntent(android.content.Context context, 
                                     String message, 
                                     Class<?> targetActivity) {
        return createIntent(context, message, targetActivity, DEFAULT_LOADING_DURATION);
    }

    // Helper method with circular progress
    public static Intent createIntentWithCircularProgress(android.content.Context context, 
                                                         String message, 
                                                         Class<?> targetActivity, 
                                                         int duration) {
        Intent intent = createIntent(context, message, targetActivity, duration);
        intent.putExtra(EXTRA_USE_CIRCULAR_PROGRESS, true);
        return intent;
    }
}