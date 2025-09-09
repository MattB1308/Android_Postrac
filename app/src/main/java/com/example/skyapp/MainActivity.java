package com.example.skyapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

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

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle, btnOutlook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnOutlook = findViewById(R.id.btnOutlook);

        btnLogin.setOnClickListener(v -> login());

        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show();
            // Aquí iría la integración con Google Sign-In
        });

        btnOutlook.setOnClickListener(v -> {
            Toast.makeText(this, "Outlook Login Clicked", Toast.LENGTH_SHORT).show();
            // Aquí iría la integración con Outlook OAuth
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato del email
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        loginUser(this, email, password);
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

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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
                progressDialog.dismiss();

                // ✅ LOG DEL RESPONSE (status + headers)
                Log.d("LOGIN_RESPONSE", "code=" + response.code() + ", message=" + response.message());
                Log.d("LOGIN_RESPONSE", "headers=" + response.headers().toString());

                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Body parseado
                    Log.d("LOGIN_RESPONSE", "body=" + new Gson().toJson(response.body()));
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show();

                    String loginResponseJson = new Gson().toJson(response.body());
                    SharedPreferences sharedPreferences = context.getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("login_response", loginResponseJson).apply();

                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    // ✅ Body de error crudo (si viene)
                    try {
                        String errorRaw = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("LOGIN_RESPONSE_ERROR", errorRaw);
                    } catch (Exception e) {
                        Log.e("LOGIN_RESPONSE_ERROR", "No se pudo leer errorBody()", e);
                    }
                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BO_response.LoginResponse> call, Throwable t) {
                progressDialog.dismiss();
                // ✅ Error de red/timeout/etc.
                Log.e("LOGIN_FAILURE", "onFailure: " + t.getMessage(), t);
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




}
