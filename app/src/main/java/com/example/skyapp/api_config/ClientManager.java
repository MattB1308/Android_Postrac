package com.example.skyapp.api_config;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientManager {
    private static final String TAG = "ClientManager";
    private static ClientManager instance;
    private final Map<ApiService, Retrofit> clients = new HashMap<>();
    private final Context context;

    private ClientManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized ClientManager getInstance(Context context) {
        if (instance == null) {
            instance = new ClientManager(context);
        }
        return instance;
    }

    /**
     * Get a Retrofit client for the specified service
     * @param service The API service enum
     * @return Configured Retrofit client
     */
    public Retrofit getClient(ApiService service) {
        if (!clients.containsKey(service)) {
            Log.d(TAG, "Creating new client for service: " + service.name() + " with URL: " + service.getBaseUrl());
            clients.put(service, createClient(service));
        }
        return clients.get(service);
    }

    /**
     * Create a new Retrofit client for the specified service
     * @param service The API service
     * @return Configured Retrofit client
     */
    private Retrofit createClient(ApiService service) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();

        return new Retrofit.Builder()
                .baseUrl(service.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Create a service interface for the specified API service
     * @param service The API service
     * @param serviceClass The service interface class
     * @param <T> The service interface type
     * @return The service interface implementation
     */
    public <T> T createService(ApiService service, Class<T> serviceClass) {
        Retrofit client = getClient(service);
        T serviceInterface = client.create(serviceClass);
        Log.d(TAG, "Created service interface: " + serviceClass.getSimpleName() + " for " + service.name());
        return serviceInterface;
    }

    /**
     * Clear all cached clients (useful for testing or configuration changes)
     */
    public void clearClients() {
        Log.d(TAG, "Clearing all cached clients");
        clients.clear();
    }

    /**
     * Get the base URL for a specific service
     * @param service The API service
     * @return The base URL string
     */
    public String getBaseUrl(ApiService service) {
        return service.getBaseUrl();
    }
}