package com.example.skyapp.bo.routing;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BO_response {

    /**
     * Response class for delivery route request
     * API: /api/routing/getDeliveryRouteByUserAndDate
     */
    public static class DeliveryRouteResponse {
        @SerializedName("data")
        private Data data;

        @SerializedName("serverInfo")
        private ServerInfo serverInfo;

        @SerializedName("logRequest")
        private LogRequest logRequest;

        // Getters and setters
        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public ServerInfo getServerInfo() {
            return serverInfo;
        }

        public void setServerInfo(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
        }

        public LogRequest getLogRequest() {
            return logRequest;
        }

        public void setLogRequest(LogRequest logRequest) {
            this.logRequest = logRequest;
        }

        // Helper method to get route IDs list
        public List<Route> getRoutes() {
            return data != null ? data.getRoutes() : null;
        }
    }

    /**
     * Data nested class containing routes
     */
    public static class Data {
        @SerializedName("routes")
        private List<Route> routes;

        // Getters and setters
        public List<Route> getRoutes() {
            return routes;
        }

        public void setRoutes(List<Route> routes) {
            this.routes = routes;
        }
    }

    /**
     * Route nested class containing routeId
     */
    public static class Route {
        @SerializedName("routeId")
        private int routeId;

        // Getters and setters
        public int getRouteId() {
            return routeId;
        }

        public void setRouteId(int routeId) {
            this.routeId = routeId;
        }
    }

    /**
     * ServerInfo nested class
     */
    public static class ServerInfo {
        @SerializedName("serverDebugInfo")
        private List<String> serverDebugInfo;

        // Getters and setters
        public List<String> getServerDebugInfo() {
            return serverDebugInfo;
        }

        public void setServerDebugInfo(List<String> serverDebugInfo) {
            this.serverDebugInfo = serverDebugInfo;
        }
    }

    /**
     * LogRequest nested class
     */
    public static class LogRequest {
        @SerializedName("logId")
        private String logId;

        // Getters and setters
        public String getLogId() {
            return logId;
        }

        public void setLogId(String logId) {
            this.logId = logId;
        }
    }

    /**
     * Response class for detailed route information by routeId
     * API: /api/routing/getDeliveryRouteInfoByRouteId
     */
    public static class RouteDetailsResponse {
        @SerializedName("data")
        private RouteDetailsData data;

        @SerializedName("serverInfo")
        private ServerInfo serverInfo;

        @SerializedName("logRequest")
        private LogRequest logRequest;

        // Getters and setters
        public RouteDetailsData getData() {
            return data;
        }

        public void setData(RouteDetailsData data) {
            this.data = data;
        }

        public ServerInfo getServerInfo() {
            return serverInfo;
        }

        public void setServerInfo(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
        }

        public LogRequest getLogRequest() {
            return logRequest;
        }

        public void setLogRequest(LogRequest logRequest) {
            this.logRequest = logRequest;
        }
    }

    /**
     * RouteDetailsData containing the route information
     */
    public static class RouteDetailsData {
        @SerializedName("route")
        private List<RouteDetail> route;

        // Getters and setters
        public List<RouteDetail> getRoute() {
            return route;
        }

        public void setRoute(List<RouteDetail> route) {
            this.route = route;
        }
    }

    /**
     * RouteDetail containing main route information
     */
    public static class RouteDetail {
        @SerializedName("data")
        private RouteMainData data;

        @SerializedName("driverInfo")
        private String driverInfo;

        @SerializedName("vehicleInfo")
        private String vehicleInfo;

        // Getters and setters
        public RouteMainData getData() {
            return data;
        }

        public void setData(RouteMainData data) {
            this.data = data;
        }

        public String getDriverInfo() {
            return driverInfo;
        }

        public void setDriverInfo(String driverInfo) {
            this.driverInfo = driverInfo;
        }

        public String getVehicleInfo() {
            return vehicleInfo;
        }

        public void setVehicleInfo(String vehicleInfo) {
            this.vehicleInfo = vehicleInfo;
        }
    }

    /**
     * RouteMainData containing the main information
     */
    public static class RouteMainData {
        @SerializedName("mainInfo")
        private MainInfo mainInfo;

        // Getters and setters
        public MainInfo getMainInfo() {
            return mainInfo;
        }

        public void setMainInfo(MainInfo mainInfo) {
            this.mainInfo = mainInfo;
        }
    }

    /**
     * MainInfo containing route details
     */
    public static class MainInfo {
        @SerializedName("routeId")
        private int routeId;

        @SerializedName("dateCreated")
        private String dateCreated;

        @SerializedName("operationalDate")
        private String operationalDate;

        @SerializedName("hubRoute")
        private String hubRoute;

        @SerializedName("initialPoint")
        private RoutePoint initialPoint;

        @SerializedName("routePoints")
        private List<RoutePoint> routePoints;

        @SerializedName("endPoint")
        private RoutePoint endPoint;

        // Getters and setters
        public int getRouteId() {
            return routeId;
        }

        public void setRouteId(int routeId) {
            this.routeId = routeId;
        }

        public String getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(String dateCreated) {
            this.dateCreated = dateCreated;
        }

        public String getOperationalDate() {
            return operationalDate;
        }

        public void setOperationalDate(String operationalDate) {
            this.operationalDate = operationalDate;
        }

        public String getHubRoute() {
            return hubRoute;
        }

        public void setHubRoute(String hubRoute) {
            this.hubRoute = hubRoute;
        }

        public RoutePoint getInitialPoint() {
            return initialPoint;
        }

        public void setInitialPoint(RoutePoint initialPoint) {
            this.initialPoint = initialPoint;
        }

        public List<RoutePoint> getRoutePoints() {
            return routePoints;
        }

        public void setRoutePoints(List<RoutePoint> routePoints) {
            this.routePoints = routePoints;
        }

        public RoutePoint getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(RoutePoint endPoint) {
            this.endPoint = endPoint;
        }
    }

    /**
     * RoutePoint representing delivery points
     */
    public static class RoutePoint {
        @SerializedName("routePointId")
        private Integer routePointId;

        @SerializedName("reRouted")
        private Boolean reRouted;

        @SerializedName("deliveryOrder")
        private Integer deliveryOrder;

        @SerializedName("consignee")
        private Consignee consignee;

        @SerializedName("routePointData")
        private List<RoutePointData> routePointData;

        @SerializedName("geoRef")
        private GeoRef geoRef;

        // Getters and setters
        public Integer getRoutePointId() {
            return routePointId;
        }

        public void setRoutePointId(Integer routePointId) {
            this.routePointId = routePointId;
        }

        public Boolean getReRouted() {
            return reRouted;
        }

        public void setReRouted(Boolean reRouted) {
            this.reRouted = reRouted;
        }

        public Integer getDeliveryOrder() {
            return deliveryOrder;
        }

        public void setDeliveryOrder(Integer deliveryOrder) {
            this.deliveryOrder = deliveryOrder;
        }

        public Consignee getConsignee() {
            return consignee;
        }

        public void setConsignee(Consignee consignee) {
            this.consignee = consignee;
        }

        public List<RoutePointData> getRoutePointData() {
            return routePointData;
        }

        public void setRoutePointData(List<RoutePointData> routePointData) {
            this.routePointData = routePointData;
        }

        public GeoRef getGeoRef() {
            return geoRef;
        }

        public void setGeoRef(GeoRef geoRef) {
            this.geoRef = geoRef;
        }
    }

    /**
     * Consignee information
     */
    public static class Consignee {
        @SerializedName("name")
        private String name;

        @SerializedName("address")
        private String address;

        @SerializedName("zipCode")
        private String zipCode;

        @SerializedName("city")
        private String city;

        @SerializedName("state")
        private String state;

        @SerializedName("phone")
        private String phone;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     * RoutePointData containing package information
     */
    public static class RoutePointData {
        @SerializedName("trackingNumber")
        private String trackingNumber;

        @SerializedName("container")
        private String container;

        @SerializedName("deliveryStatus")
        private int deliveryStatus;

        @SerializedName("deliveryStatusDescription")
        private String deliveryStatusDescription;

        // Getters and setters
        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getContainer() {
            return container;
        }

        public void setContainer(String container) {
            this.container = container;
        }

        public int getDeliveryStatus() {
            return deliveryStatus;
        }

        public void setDeliveryStatus(int deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
        }

        public String getDeliveryStatusDescription() {
            return deliveryStatusDescription;
        }

        public void setDeliveryStatusDescription(String deliveryStatusDescription) {
            this.deliveryStatusDescription = deliveryStatusDescription;
        }
    }

    /**
     * GeoRef containing coordinates
     */
    public static class GeoRef {
        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        // Getters and setters
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
    }

}