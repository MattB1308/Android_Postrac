package com.example.skyapp.bo.routing;

import java.util.List;

/**
 * Extensions to BO_request for local storage and route management
 */
public class BO_request_extensions {
    
    /**
     * Route data class for storing route information locally
     */
    public static class RouteData {
        private List<RoutePoint> routePoints;
        
        public RouteData() {}
        
        public List<RoutePoint> getRoutePoints() {
            return routePoints;
        }
        
        public void setRoutePoints(List<RoutePoint> routePoints) {
            this.routePoints = routePoints;
        }
    }
    
    /**
     * Route point class for storing individual route points
     */
    public static class RoutePoint {
        private double latitude;
        private double longitude;
        private Consignee consignee;
        
        public RoutePoint() {}
        
        public double getLatitude() {
            return latitude;
        }
        
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
        
        public Consignee getConsignee() {
            return consignee;
        }
        
        public void setConsignee(Consignee consignee) {
            this.consignee = consignee;
        }
        
        /**
         * Consignee nested class for RoutePoint
         */
        public static class Consignee {
            private String name;
            private String phone;
            private String address;
            
            public Consignee() {}
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public String getPhone() {
                return phone;
            }
            
            public void setPhone(String phone) {
                this.phone = phone;
            }
            
            public String getAddress() {
                return address;
            }
            
            public void setAddress(String address) {
                this.address = address;
            }
        }
    }
}