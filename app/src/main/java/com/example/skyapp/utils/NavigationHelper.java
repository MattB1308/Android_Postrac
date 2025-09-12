package com.example.skyapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.example.skyapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NavigationHelper {
    
    private static final String TAG = "NavigationHelper";
    
    public static class NavigationApp {
        public String name;
        public String packageName;
        public String intentAction;
        public int iconRes;
        public String description;
        public int backgroundColor;
        
        public NavigationApp(String name, String packageName, String intentAction, int iconRes, String description, int backgroundColor) {
            this.name = name;
            this.packageName = packageName;
            this.intentAction = intentAction;
            this.iconRes = iconRes;
            this.description = description;
            this.backgroundColor = backgroundColor;
        }
    }
    
    /**
     * Show navigation app selector dialog with elegant design
     */
    public static void showNavigationDialog(Context context, double destLat, double destLng, String destinationName) {
        List<NavigationApp> availableApps = getAvailableNavigationApps(context);
        
        if (availableApps.isEmpty()) {
            // No navigation apps available, open in browser
            openInBrowser(context, destLat, destLng);
            return;
        }
        
        // Create custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_navigation_selector, null);
        
        // Setup destination text
        TextView txtDestination = dialogView.findViewById(R.id.txtDestination);
        if (destinationName != null && !destinationName.isEmpty()) {
            txtDestination.setText("Navigate to: " + destinationName);
        }
        
        // Create and show dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Setup apps container
        LinearLayout appsContainer = dialogView.findViewById(R.id.navigationAppsContainer);
        
        // Add each available app
        for (NavigationApp app : availableApps) {
            View appView = inflater.inflate(R.layout.item_navigation_app, appsContainer, false);
            
            ImageView appIcon = appView.findViewById(R.id.appIcon);
            TextView appName = appView.findViewById(R.id.appName);
            TextView appDescription = appView.findViewById(R.id.appDescription);
            View appIconBackground = appView.findViewById(R.id.appIconBackground);
            
            appIcon.setImageResource(app.iconRes);
            appName.setText(app.name);
            appDescription.setText(app.description);
            
            // Set app-specific background color
            appIconBackground.setBackgroundResource(app.backgroundColor);
            
            // Set click listener
            appView.setOnClickListener(v -> {
                dialog.dismiss();
                openNavigationApp(context, app, destLat, destLng, destinationName);
            });
            
            appsContainer.addView(appView);
        }
        
        // Setup cancel button
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * Get list of available navigation apps with enhanced descriptions
     */
    private static List<NavigationApp> getAvailableNavigationApps(Context context) {
        List<NavigationApp> apps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        
        // Google Maps
        if (isAppInstalled(pm, "com.google.android.apps.maps")) {
            apps.add(new NavigationApp(
                "Google Maps", 
                "com.google.android.apps.maps", 
                "google.navigation", 
                android.R.drawable.ic_dialog_map,
                "Navigate with turn-by-turn directions",
                R.drawable.circle_sepex_blue_light
            ));
        }
        
        // Waze - Enhanced support with multiple package names
        String wazePackage = getWazePackageName(pm);
        if (wazePackage != null) {
            apps.add(new NavigationApp(
                "Waze", 
                wazePackage, 
                "waze", 
                android.R.drawable.ic_dialog_map,
                "Community-based traffic and navigation",
                R.drawable.circle_sepex_red_light
            ));
        }
        
        // HERE WeGo
        if (isAppInstalled(pm, "com.here.app.maps")) {
            apps.add(new NavigationApp(
                "HERE WeGo", 
                "com.here.app.maps", 
                "here.maps", 
                android.R.drawable.ic_dialog_map,
                "Offline maps and navigation",
                R.drawable.circle_sepex_blue_light
            ));
        }
        
        Log.d(TAG, "Found " + apps.size() + " navigation apps available");
        return apps;
    }
    
    /**
     * Get Waze package name - checks for different Waze variants
     */
    private static String getWazePackageName(PackageManager pm) {
        String[] wazePackages = {
            "com.waze",                    // Standard Waze
            "com.waze.debug",              // Debug version
            "com.waze.beta",               // Beta version
            "com.waze.carplay",            // CarPlay version
            "com.google.android.apps.waze" // Possible Google Play variant
        };
        
        for (String packageName : wazePackages) {
            if (isAppInstalled(pm, packageName)) {
                Log.d(TAG, "Found Waze with package: " + packageName);
                return packageName;
            }
        }
        
        Log.d(TAG, "Waze not found with any known package name");
        return null;
    }
    
    /**
     * Check if app is installed
     */
    private static boolean isAppInstalled(PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Open specific navigation app with enhanced error handling
     */
    private static void openNavigationApp(Context context, NavigationApp app, 
                                        double destLat, double destLng, String destinationName) {
        
        try {
            Intent intent = null;
            
            switch (app.packageName) {
                case "com.google.android.apps.maps":
                    // Google Maps navigation - multiple fallback options
                    try {
                        // Primary: Google Navigation
                        Uri gmmIntentUri = Uri.parse(String.format(Locale.US, 
                                "google.navigation:q=%f,%f&mode=d", destLat, destLng));
                        intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        intent.setPackage("com.google.android.apps.maps");
                        
                        // Test if this works
                        if (intent.resolveActivity(context.getPackageManager()) == null) {
                            // Fallback: Maps with directions
                            Uri mapsUri = Uri.parse(String.format(Locale.US, 
                                    "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=driving", 
                                    destLat, destLng));
                            intent = new Intent(Intent.ACTION_VIEW, mapsUri);
                            intent.setPackage("com.google.android.apps.maps");
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Google Maps primary intent failed, using fallback");
                        Uri mapsUri = Uri.parse(String.format(Locale.US, 
                                "geo:%f,%f?q=%f,%f", destLat, destLng, destLat, destLng));
                        intent = new Intent(Intent.ACTION_VIEW, mapsUri);
                        intent.setPackage("com.google.android.apps.maps");
                    }
                    break;
                    
                case "com.waze":
                    // Waze navigation - Enhanced with multiple options
                    try {
                        // Primary: Waze with navigate parameter
                        Uri wazeUri = Uri.parse(String.format(Locale.US, 
                                "waze://?ll=%f,%f&navigate=yes", destLat, destLng));
                        intent = new Intent(Intent.ACTION_VIEW, wazeUri);
                        intent.setPackage("com.waze");
                        
                        // Test if this works
                        if (intent.resolveActivity(context.getPackageManager()) == null) {
                            // Fallback 1: Waze without navigate parameter
                            wazeUri = Uri.parse(String.format(Locale.US, 
                                    "waze://?ll=%f,%f", destLat, destLng));
                            intent = new Intent(Intent.ACTION_VIEW, wazeUri);
                            intent.setPackage("com.waze");
                        }
                        
                        if (intent.resolveActivity(context.getPackageManager()) == null) {
                            // Fallback 2: Waze with different format
                            wazeUri = Uri.parse(String.format(Locale.US, 
                                    "https://waze.com/ul?ll=%f,%f&navigate=yes", destLat, destLng));
                            intent = new Intent(Intent.ACTION_VIEW, wazeUri);
                            intent.setPackage("com.waze");
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Waze intent failed: " + e.getMessage());
                        // Final fallback for Waze
                        Uri wazeUri = Uri.parse(String.format(Locale.US, 
                                "geo:%f,%f?q=%f,%f", destLat, destLng, destLat, destLng));
                        intent = new Intent(Intent.ACTION_VIEW, wazeUri);
                        intent.setPackage("com.waze");
                    }
                    break;
                    
                case "com.here.app.maps":
                    // HERE WeGo navigation
                    try {
                        Uri hereUri = Uri.parse(String.format(Locale.US, 
                                "here.directions://v1.0/mylocation/%f,%f", destLat, destLng));
                        intent = new Intent(Intent.ACTION_VIEW, hereUri);
                        intent.setPackage("com.here.app.maps");
                        
                        // Fallback for HERE
                        if (intent.resolveActivity(context.getPackageManager()) == null) {
                            hereUri = Uri.parse(String.format(Locale.US, 
                                    "geo:%f,%f?q=%f,%f", destLat, destLng, destLat, destLng));
                            intent = new Intent(Intent.ACTION_VIEW, hereUri);
                            intent.setPackage("com.here.app.maps");
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "HERE WeGo intent failed: " + e.getMessage());
                        Uri hereUri = Uri.parse(String.format(Locale.US, 
                                "geo:%f,%f?q=%f,%f", destLat, destLng, destLat, destLng));
                        intent = new Intent(Intent.ACTION_VIEW, hereUri);
                        intent.setPackage("com.here.app.maps");
                    }
                    break;
            }
            
            if (intent != null && intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Log.d(TAG, "Successfully opened navigation in " + app.name + " to coordinates: " + destLat + ", " + destLng);
            } else {
                Log.w(TAG, "Could not open " + app.name + ", falling back to browser");
                openInBrowser(context, destLat, destLng);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening navigation app " + app.name + ": " + e.getMessage());
            openInBrowser(context, destLat, destLng);
        }
    }
    
    /**
     * Open navigation in web browser as fallback
     */
    private static void openInBrowser(Context context, double destLat, double destLng) {
        try {
            String url = String.format(Locale.US, 
                    "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=driving", 
                    destLat, destLng);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
            Log.d(TAG, "Opened navigation in browser");
        } catch (Exception e) {
            Log.e(TAG, "Error opening browser navigation: " + e.getMessage());
        }
    }
    
    /**
     * Navigate to next delivery using current location
     */
    public static void navigateToNextDelivery(Context context, double destLat, double destLng, 
                                            String destinationAddress) {
        showNavigationDialog(context, destLat, destLng, destinationAddress);
    }
}