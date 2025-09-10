package com.example.skyapp.api_config;

import android.content.Context;
import android.util.Log;

import retrofit2.Retrofit;

/**
 * Legacy client class - Maintained for backward compatibility
 * For new implementations, use ClientManager.getInstance(context).getClient(ApiService.SERVICE_NAME)
 */
public class client {
    private static final String TAG = "LegacyClient";

    /**
     * Get client for login service (backward compatibility)
     * @param context Application context
     * @return Retrofit client configured for login service
     * @deprecated Use ClientManager.getInstance(context).getClient(ApiService.LOGIN) instead
     */
    @Deprecated
    public static Retrofit getClient(Context context) {
        Log.w(TAG, "Using deprecated getClient method. Consider migrating to ClientManager.");
        return ClientManager.getInstance(context).getClient(ApiService.LOGIN);
    }

    /**
     * Get client for specific service
     * @param context Application context
     * @param service The API service to get client for
     * @return Retrofit client configured for the specified service
     */
    public static Retrofit getClient(Context context, ApiService service) {
        Log.d(TAG, "Getting client for service: " + service.name());
        return ClientManager.getInstance(context).getClient(service);
    }

    /**
     * Create service interface for specific API service
     * @param context Application context
     * @param service The API service
     * @param serviceClass The service interface class
     * @param <T> The service interface type
     * @return The service interface implementation
     */
    public static <T> T createService(Context context, ApiService service, Class<T> serviceClass) {
        Log.d(TAG, "Creating service: " + serviceClass.getSimpleName() + " for " + service.name());
        return ClientManager.getInstance(context).createService(service, serviceClass);
    }
}
