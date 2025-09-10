package com.example.skyapp.bo.routing;

import com.google.gson.annotations.SerializedName;

public class BO_request {

    /**
     * Request class for getting delivery route by user and date
     * API: /api/routing/getDeliveryRouteByUserAndDate
     */
    public static class DeliveryRouteRequest {
        @SerializedName("userInfo")
        private UserInfo userInfo;

        @SerializedName("rteUserId")
        private int rteUserId;

        @SerializedName("rteDate")
        private String rteDate;

        public DeliveryRouteRequest(UserInfo userInfo, int rteUserId, String rteDate) {
            this.userInfo = userInfo;
            this.rteUserId = rteUserId;
            this.rteDate = rteDate;
        }

        // Getters and setters
        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        public int getRteUserId() {
            return rteUserId;
        }

        public void setRteUserId(int rteUserId) {
            this.rteUserId = rteUserId;
        }

        public String getRteDate() {
            return rteDate;
        }

        public void setRteDate(String rteDate) {
            this.rteDate = rteDate;
        }
    }

    /**
     * UserInfo nested class for the request
     */
    public static class UserInfo {
        @SerializedName("user")
        private User user;

        @SerializedName("application")
        private Application application;

        public UserInfo(User user, Application application) {
            this.user = user;
            this.application = application;
        }

        // Getters and setters
        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public Application getApplication() {
            return application;
        }

        public void setApplication(Application application) {
            this.application = application;
        }
    }

    /**
     * User nested class
     */
    public static class User {
        @SerializedName("userId")
        private int userId;

        @SerializedName("userUuid")
        private String userUuid;

        public User(int userId, String userUuid) {
            this.userId = userId;
            this.userUuid = userUuid;
        }

        // Getters and setters
        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getUserUuid() {
            return userUuid;
        }

        public void setUserUuid(String userUuid) {
            this.userUuid = userUuid;
        }
    }

    /**
     * Application nested class
     */
    public static class Application {
        @SerializedName("appKey")
        private String appKey;

        public Application(String appKey) {
            this.appKey = appKey;
        }

        // Getters and setters
        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }
    }

    /**
     * Request class for getting detailed route information by routeId
     * API: /api/routing/getDeliveryRouteInfoByRouteId
     */
    public static class RouteDetailsRequest {
        @SerializedName("userInfo")
        private UserInfo userInfo;

        @SerializedName("routeId")
        private int routeId;

        public RouteDetailsRequest(UserInfo userInfo, int routeId) {
            this.userInfo = userInfo;
            this.routeId = routeId;
        }

        // Getters and setters
        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        public int getRouteId() {
            return routeId;
        }

        public void setRouteId(int routeId) {
            this.routeId = routeId;
        }
    }
}