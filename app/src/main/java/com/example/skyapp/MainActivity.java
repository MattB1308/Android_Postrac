package com.example.skyapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.card.MaterialCardView;

import com.example.skyapp.api_config.login.LoginInterface;
import com.example.skyapp.api_config.client;
import com.example.skyapp.api_config.login.LoginInterface;
import com.example.skyapp.bo.login.BO_request;
import com.example.skyapp.bo.login.BO_response;
import com.example.skyapp.ui.MapsActivity;
//API CALL
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.Gson;
//LOCAL REALM DB


public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle, btnOutlook;
    private TextView tvForgotPassword, tvSignUp;
    private MaterialCardView loginCard;
    private View logoContainer;
    private View decorativeCircleBlue1, decorativeCircleRed1, decorativeCircleBlue2, decorativeCircleRed2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnOutlook = findViewById(R.id.btnOutlook);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        loginCard = findViewById(R.id.loginCard);
        logoContainer = findViewById(R.id.logoContainer);
        
        // Initialize decorative elements
        decorativeCircleBlue1 = findViewById(R.id.decorativeCircleBlue1);
        decorativeCircleRed1 = findViewById(R.id.decorativeCircleRed1);
        decorativeCircleBlue2 = findViewById(R.id.decorativeCircleBlue2);
        decorativeCircleRed2 = findViewById(R.id.decorativeCircleRed2);

        // Setup animations
        setupEntranceAnimations();

        // Set click listeners
        btnLogin.setOnClickListener(v -> login());

        btnGoogle.setOnClickListener(v -> {
            animateButtonClick(btnGoogle);
            Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Integrate Google Sign-In
        });

        btnOutlook.setOnClickListener(v -> {
            animateButtonClick(btnOutlook);
            Toast.makeText(this, "Microsoft Login Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Integrate Microsoft OAuth
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement forgot password functionality
        });

        tvSignUp.setOnClickListener(v -> {
            Toast.makeText(this, "Sign Up Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to sign up activity
        });
    }

    private void setupEntranceAnimations() {
        // Initially hide views for animation
        logoContainer.setAlpha(0f);
        logoContainer.setScaleX(0.3f);
        logoContainer.setScaleY(0.3f);
        loginCard.setAlpha(0f);
        loginCard.setTranslationY(100f);

        // Hide decorative circles initially
        decorativeCircleBlue1.setAlpha(0f);
        decorativeCircleRed1.setAlpha(0f);
        decorativeCircleBlue2.setAlpha(0f);
        decorativeCircleRed2.setAlpha(0f);

        // Animate decorative circles with staggered entrance
        decorativeCircleBlue1.animate()
                .alpha(0.3f)
                .setDuration(1200)
                .setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        decorativeCircleRed1.animate()
                .alpha(0.25f)
                .setDuration(1000)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        decorativeCircleBlue2.animate()
                .alpha(0.2f)
                .setDuration(800)
                .setStartDelay(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        decorativeCircleRed2.animate()
                .alpha(0.15f)
                .setDuration(600)
                .setStartDelay(700)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate logo entrance with scale and fade
        logoContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate card entrance with delay
        loginCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(800)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Start continuous floating animation for decorative circles
        startFloatingAnimation();
    }

    private void startFloatingAnimation() {
        // Floating animation for blue circles
        ObjectAnimator floatBlue1 = ObjectAnimator.ofFloat(decorativeCircleBlue1, "translationY", 0f, -20f, 0f);
        floatBlue1.setDuration(4000);
        floatBlue1.setRepeatCount(ObjectAnimator.INFINITE);
        floatBlue1.setInterpolator(new AccelerateDecelerateInterpolator());
        floatBlue1.setStartDelay(1000);
        floatBlue1.start();

        ObjectAnimator floatBlue2 = ObjectAnimator.ofFloat(decorativeCircleBlue2, "translationY", 0f, 15f, 0f);
        floatBlue2.setDuration(3500);
        floatBlue2.setRepeatCount(ObjectAnimator.INFINITE);
        floatBlue2.setInterpolator(new AccelerateDecelerateInterpolator());
        floatBlue2.setStartDelay(1500);
        floatBlue2.start();

        // Floating animation for red circles
        ObjectAnimator floatRed1 = ObjectAnimator.ofFloat(decorativeCircleRed1, "translationX", 0f, 10f, 0f);
        floatRed1.setDuration(5000);
        floatRed1.setRepeatCount(ObjectAnimator.INFINITE);
        floatRed1.setInterpolator(new AccelerateDecelerateInterpolator());
        floatRed1.setStartDelay(2000);
        floatRed1.start();

        ObjectAnimator floatRed2 = ObjectAnimator.ofFloat(decorativeCircleRed2, "translationX", 0f, -8f, 0f);
        floatRed2.setDuration(4500);
        floatRed2.setRepeatCount(ObjectAnimator.INFINITE);
        floatRed2.setInterpolator(new AccelerateDecelerateInterpolator());
        floatRed2.setStartDelay(2500);
        floatRed2.start();
    }

    private void animateButtonClick(View view) {
        // Scale animation for button feedback
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 0.95f);
        scaleDown.setDuration(100);
        
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        scaleUp.setDuration(100);
        
        scaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleUp.start();
            }
        });
        
        scaleDown.start();
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Please fill in all fields");
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            showErrorMessage("Please enter a valid email");
            return;
        }

        // Animate button loading state
        animateButtonLoading(btnLogin, true);
        loginUser(this, email, password);
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Shake animation for error feedback
        shakeView(loginCard);
    }

    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(600);
        shake.start();
    }

    private void animateButtonLoading(MaterialButton button, boolean isLoading) {
        if (isLoading) {
            button.setText("Signing in...");
            button.setEnabled(false);
            // Add subtle pulse animation while loading
            ObjectAnimator pulse = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.7f, 1f);
            pulse.setDuration(500);
            pulse.setRepeatCount(ObjectAnimator.INFINITE);
            pulse.start();
            button.setTag(pulse); // Store reference to cancel later
        } else {
            button.setText(getString(R.string.login_button_text));
            button.setEnabled(true);
            // Cancel pulse animation
            ObjectAnimator pulseAnim = (ObjectAnimator) button.getTag();
            if (pulseAnim != null) {
                pulseAnim.cancel();
                button.setAlpha(1f);
            }
        }
    }

    private void showSuccessAnimation() {
        // Change login button to success state temporarily
        btnLogin.setText("Success!");
        btnLogin.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        
        // Scale up animation for success feedback
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(loginCard, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(loginCard, "scaleY", 1f, 1.05f, 1f);
        scaleUpX.setDuration(600);
        scaleUpY.setDuration(600);
        scaleUpX.start();
        scaleUpY.start();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void loginUser(Context context, String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use modern Material Design progress indicator instead of deprecated ProgressDialog
        LoginInterface apiService = client.getClient(context).create(LoginInterface.class);

        // 🔹 Crea el request
        BO_request.LoginRequest request = new BO_request.LoginRequest(email, password, "83d6661f-9f64-43c4-b672-cdcab3a57685");

        // ✅ LOG DEL REQUEST (lo que mandas)
        Log.d("LOGIN_REQUEST", "-> " + new Gson().toJson(request));

        Call<BO_response.LoginResponse> call = apiService.login(request);
        // Antes de ejecutar la llamada
        Log.d("LOGIN_URL", "Request URL: " + call.request().url().toString());

        call.enqueue(new Callback<BO_response.LoginResponse>() {
            @Override
            public void onResponse(Call<BO_response.LoginResponse> call, Response<BO_response.LoginResponse> response) {
                // Reset button loading state
                if (context instanceof MainActivity) {
                    ((MainActivity) context).animateButtonLoading(((MainActivity) context).btnLogin, false);
                }

                // ✅ LOG DEL RESPONSE (status + headers)
                Log.d("LOGIN_RESPONSE", "code=" + response.code() + ", message=" + response.message());
                Log.d("LOGIN_RESPONSE", "headers=" + response.headers().toString());

                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Body parseado
                    Log.d("LOGIN_RESPONSE", "body=" + new Gson().toJson(response.body()));
                    
                    // Show success animation before navigation
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showSuccessAnimation();
                    }
                    
                    String loginResponseJson = new Gson().toJson(response.body());
                    SharedPreferences sharedPreferences = context.getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("login_response", loginResponseJson).apply();

                    // Delay navigation to show success animation, then show loading screen
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        // Create intent for loading screen that will navigate to MapsActivity
                        Intent loadingIntent = LoadingActivity.createIntent(
                            context,
                            context.getString(R.string.loading_login_message),
                            MapsActivity.class,
                            2000  // 4 seconds loading time
                        );
                        loadingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(loadingIntent);
                        
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }, 1000);
                } else {
                    // ✅ Body de error crudo (si viene)
                    try {
                        String errorRaw = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("LOGIN_RESPONSE_ERROR", errorRaw);
                    } catch (Exception e) {
                        Log.e("LOGIN_RESPONSE_ERROR", "No se pudo leer errorBody()", e);
                    }
                    
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showErrorMessage("Invalid credentials");
                    } else {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BO_response.LoginResponse> call, Throwable t) {
                // Reset button loading state
                if (context instanceof MainActivity) {
                    ((MainActivity) context).animateButtonLoading(((MainActivity) context).btnLogin, false);
                }
                
                // ✅ Error de red/timeout/etc.
                Log.e("LOGIN_FAILURE", "onFailure: " + t.getMessage(), t);
                
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showErrorMessage("Network error: " + t.getMessage());
                } else {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




}
