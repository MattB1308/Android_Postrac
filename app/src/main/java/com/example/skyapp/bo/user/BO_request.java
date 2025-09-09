package com.example.skyapp.bo.user;

import com.google.gson.annotations.SerializedName;

public class BO_request {

    public static class ProfileRequest {
        @SerializedName("userInfo")
        private UserInfo userInfo;

        public ProfileRequest(int userId, String userUuid, String appKey) {
            this.userInfo = new UserInfo(userId, userUuid, appKey);
        }

        public static class UserInfo {
            @SerializedName("user")
            private User user;

            @SerializedName("application")
            private Application application;

            public UserInfo(int userId, String userUuid, String appKey) {
                this.user = new User(userId, userUuid);
                this.application = new Application(appKey);
            }

            public static class User {
                @SerializedName("userId")
                private int userId;

                @SerializedName("userUuid")
                private String userUuid;

                public User(int userId, String userUuid) {
                    this.userId = userId;
                    this.userUuid = userUuid;
                }
            }

            public static class Application {
                @SerializedName("appKey")
                private String appKey;

                public Application(String appKey) {
                    this.appKey = appKey;
                }
            }
        }
    }
}
