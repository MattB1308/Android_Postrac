package com.example.skyapp.realm;

import android.content.Context;
import android.util.Log;

import com.example.skyapp.bo.routing.BO_request_extensions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmManager {
    
    private static final String TAG = "RealmManager";
    private static RealmManager instance;
    private Realm realm;
    
    private RealmManager(Context context) {
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("skyapp.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded() // For development, remove in production
                .build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();
    }
    
    public static synchronized RealmManager getInstance(Context context) {
        if (instance == null) {
            instance = new RealmManager(context);
        }
        return instance;
    }
    
    /**
     * Save route data and packages to Realm database
     */
    public void saveRouteData(String routeId, BO_request_extensions.RouteData routeData) {
        if (routeData == null || routeData.getRoutePoints() == null) return;
        
        realm.executeTransaction(realm -> {
            // Delete existing packages for this route
            realm.where(DeliveryPackageRealm.class)
                    .equalTo("routeId", routeId)
                    .findAll()
                    .deleteAllFromRealm();
            
            // Save new packages
            int order = 1;
            for (BO_request_extensions.RoutePoint point : routeData.getRoutePoints()) {
                if (point.getConsignee() != null) {
                    DeliveryPackageRealm packageRealm = new DeliveryPackageRealm(
                            generateTrackingNumber(point, order),
                            point.getConsignee().getName(),
                            point.getConsignee().getPhone(),
                            point.getConsignee().getAddress(),
                            point.getLatitude(),
                            point.getLongitude(),
                            "2.5", // Default weight, can be updated later
                            order,
                            routeId
                    );
                    
                    realm.copyToRealm(packageRealm);
                    order++;
                }
            }
            
            Log.d(TAG, "Saved " + (order - 1) + " packages for route: " + routeId);
        });
    }
    
    /**
     * Get next pending delivery package
     */
    public DeliveryPackageRealm getNextPendingDelivery(String routeId) {
        return realm.where(DeliveryPackageRealm.class)
                .equalTo("routeId", routeId)
                .equalTo("deliveryStatus", 0) // Pending status
                .sort("deliveryOrder", Sort.ASCENDING)
                .findFirst();
    }
    
    /**
     * Get all packages for a route, ordered by delivery order
     */
    public RealmResults<DeliveryPackageRealm> getAllPackagesForRoute(String routeId) {
        return realm.where(DeliveryPackageRealm.class)
                .equalTo("routeId", routeId)
                .sort("deliveryOrder", Sort.ASCENDING)
                .findAll();
    }
    
    /**
     * Update delivery status of a package
     */
    public boolean updateDeliveryStatus(String trackingNumber, int status, String notes) {
        DeliveryPackageRealm packageRealm = realm.where(DeliveryPackageRealm.class)
                .equalTo("trackingNumber", trackingNumber)
                .findFirst();
        
        if (packageRealm != null) {
            realm.executeTransaction(realm -> {
                packageRealm.setDeliveryStatus(status);
                packageRealm.setDeliveryNotes(notes != null ? notes : "");
                packageRealm.setDeliveryTimestamp(System.currentTimeMillis());
            });
            Log.d(TAG, "Updated package " + trackingNumber + " to status: " + status);
            return true;
        }
        
        return false;
    }
    
    /**
     * Find package by tracking number
     */
    public DeliveryPackageRealm findPackageByTracking(String trackingNumber) {
        return realm.where(DeliveryPackageRealm.class)
                .equalTo("trackingNumber", trackingNumber)
                .findFirst();
    }
    
    /**
     * Get delivery statistics
     */
    public DeliveryStats getDeliveryStats(String routeId) {
        RealmResults<DeliveryPackageRealm> allPackages = getAllPackagesForRoute(routeId);
        
        int total = allPackages.size();
        int pending = (int) allPackages.where().equalTo("deliveryStatus", 0).count();
        int delivered = (int) allPackages.where().equalTo("deliveryStatus", 1).count();
        int exceptions = (int) allPackages.where().equalTo("deliveryStatus", 2).count();
        
        return new DeliveryStats(total, pending, delivered, exceptions);
    }
    
    /**
     * Generate tracking number from route point data
     */
    private String generateTrackingNumber(BO_request_extensions.RoutePoint point, int order) {
        // Generate a tracking number based on coordinates and order
        String dateStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return "SPEX" + dateStr + String.format("%03d", order) + 
               String.valueOf(Math.abs((int)(point.getLatitude() * 1000) % 1000));
    }
    
    /**
     * Close Realm instance
     */
    public void close() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
    
    /**
     * Statistics class for delivery data
     */
    public static class DeliveryStats {
        private int total;
        private int pending;
        private int delivered;
        private int exceptions;
        
        public DeliveryStats(int total, int pending, int delivered, int exceptions) {
            this.total = total;
            this.pending = pending;
            this.delivered = delivered;
            this.exceptions = exceptions;
        }
        
        // Getters
        public int getTotal() { return total; }
        public int getPending() { return pending; }
        public int getDelivered() { return delivered; }
        public int getExceptions() { return exceptions; }
    }
}