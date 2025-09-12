package com.example.skyapp.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DeliveryPackageRealm extends RealmObject {
    
    @PrimaryKey
    private String trackingNumber;
    private String consigneeName;
    private String consigneePhone;
    private String consigneeAddress;
    private double latitude;
    private double longitude;
    private String weight;
    private int deliveryOrder; // Order in which packages should be delivered
    private int deliveryStatus; // 0 = Pending, 1 = Delivered, 2 = Exception
    private String deliveryNotes;
    private long deliveryTimestamp;
    private String routeId;
    
    // Default constructor required by Realm
    public DeliveryPackageRealm() {}
    
    public DeliveryPackageRealm(String trackingNumber, String consigneeName, String consigneePhone,
                               String consigneeAddress, double latitude, double longitude,
                               String weight, int deliveryOrder, String routeId) {
        this.trackingNumber = trackingNumber;
        this.consigneeName = consigneeName;
        this.consigneePhone = consigneePhone;
        this.consigneeAddress = consigneeAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weight = weight;
        this.deliveryOrder = deliveryOrder;
        this.deliveryStatus = 0; // Default to pending
        this.routeId = routeId;
        this.deliveryTimestamp = 0;
    }
    
    // Getters and Setters
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getConsigneeName() { return consigneeName; }
    public void setConsigneeName(String consigneeName) { this.consigneeName = consigneeName; }
    
    public String getConsigneePhone() { return consigneePhone; }
    public void setConsigneePhone(String consigneePhone) { this.consigneePhone = consigneePhone; }
    
    public String getConsigneeAddress() { return consigneeAddress; }
    public void setConsigneeAddress(String consigneeAddress) { this.consigneeAddress = consigneeAddress; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    
    public int getDeliveryOrder() { return deliveryOrder; }
    public void setDeliveryOrder(int deliveryOrder) { this.deliveryOrder = deliveryOrder; }
    
    public int getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(int deliveryStatus) { 
        this.deliveryStatus = deliveryStatus;
        if (deliveryStatus == 1 || deliveryStatus == 2) {
            this.deliveryTimestamp = System.currentTimeMillis();
        }
    }
    
    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    
    public long getDeliveryTimestamp() { return deliveryTimestamp; }
    public void setDeliveryTimestamp(long deliveryTimestamp) { this.deliveryTimestamp = deliveryTimestamp; }
    
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    
    public String getDeliveryStatusText() {
        switch (deliveryStatus) {
            case 0: return "Pending";
            case 1: return "Delivered";
            case 2: return "Exception";
            default: return "Unknown";
        }
    }
}