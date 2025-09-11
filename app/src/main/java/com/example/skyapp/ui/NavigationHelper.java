package com.example.skyapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.skyapp.LoadingActivity;
import com.example.skyapp.R;

public class NavigationHelper {

    /**
     * Setup navigation for any activity
     * @param activity The current activity
     * @param currentActivityClass The class of the current activity to avoid self-navigation
     */
    public static void setupNavigation(Activity activity, Class<?> currentActivityClass) {
        LinearLayout btnProfile = activity.findViewById(R.id.btn_profile);
        LinearLayout btnShipment = activity.findViewById(R.id.btn_shipment);
        LinearLayout btnRoute = activity.findViewById(R.id.btn_route);
        LinearLayout btnCheckpoint = activity.findViewById(R.id.btn_checkpoint);

        // Profile Navigation
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                if (currentActivityClass == ProfileActivity.class) {
                    Toast.makeText(activity, "You're already in Profile", Toast.LENGTH_SHORT).show();
                } else {
                    Intent loadingIntent = LoadingActivity.createIntent(
                        activity,
                        activity.getString(R.string.loading_profile_message),
                        ProfileActivity.class,
                        1500
                    );
                    activity.startActivity(loadingIntent);
                }
            });
        }

        // Shipments Navigation
        if (btnShipment != null) {
            btnShipment.setOnClickListener(v -> {
                if (currentActivityClass == ShipmentsActivity.class) {
                    Toast.makeText(activity, "You're already in Shipments", Toast.LENGTH_SHORT).show();
                } else {
                    Intent loadingIntent = LoadingActivity.createIntent(
                        activity,
                        activity.getString(R.string.loading_shipments_message),
                        ShipmentsActivity.class,
                        3000
                    );
                    activity.startActivity(loadingIntent);
                }
            });
        }

        // Routes Navigation (now goes to MapsActivity)
        if (btnRoute != null) {
            btnRoute.setOnClickListener(v -> {
                if (currentActivityClass == MapsActivity.class) {
                    Toast.makeText(activity, "You're already in Routes", Toast.LENGTH_SHORT).show();
                } else {
                    Intent loadingIntent = LoadingActivity.createIntent(
                        activity,
                        activity.getString(R.string.loading_routes_message),
                        MapsActivity.class,
                        2800
                    );
                    activity.startActivity(loadingIntent);
                }
            });
        }

        // Checkpoints Navigation
        if (btnCheckpoint != null) {
            btnCheckpoint.setOnClickListener(v -> {
                if (currentActivityClass == CheckpointActivity.class) {
                    Toast.makeText(activity, "You're already in Checkpoints", Toast.LENGTH_SHORT).show();
                } else {
                    Intent loadingIntent = LoadingActivity.createIntent(
                        activity,
                        activity.getString(R.string.loading_checkpoints_message),
                        CheckpointActivity.class,
                        3200
                    );
                    activity.startActivity(loadingIntent);
                }
            });
        }
    }

    /**
     * Highlight the current section in navigation
     * @param activity The current activity
     * @param currentActivityClass The class of the current activity
     */
    public static void highlightCurrentSection(Activity activity, Class<?> currentActivityClass) {
        // Reset all highlights
        resetNavigationHighlights(activity);

        // Highlight current section
        if (currentActivityClass == ProfileActivity.class) {
            highlightNavigationButton(activity, R.id.btn_profile);
        } else if (currentActivityClass == ShipmentsActivity.class) {
            highlightNavigationButton(activity, R.id.btn_shipment);
        } else if (currentActivityClass == MapsActivity.class) {
            highlightNavigationButton(activity, R.id.btn_route);
        } else if (currentActivityClass == CheckpointActivity.class) {
            highlightNavigationButton(activity, R.id.btn_checkpoint);
        }
    }

    private static void resetNavigationHighlights(Activity activity) {
        // This could be expanded to reset visual states if needed
    }

    private static void highlightNavigationButton(Activity activity, int buttonId) {
        View button = activity.findViewById(buttonId);
        if (button != null) {
            // Add visual highlight if needed
            button.setAlpha(1.0f);
        }
    }
}