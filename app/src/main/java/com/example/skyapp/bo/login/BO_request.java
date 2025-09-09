package com.example.skyapp.bo.login;

import com.google.gson.annotations.SerializedName;

public class BO_request {

    public static class LoginRequest {
        @SerializedName("userLoginInfo")
        private UserLoginInfo userLoginInfo;

        public LoginRequest(String userName, String password, String appKey) {
            this.userLoginInfo = new UserLoginInfo(userName, password, appKey);
        }

        public static class UserLoginInfo {
            @SerializedName("userName")
            private String userName;

            @SerializedName("password")
            private String password;

            @SerializedName("application")
            private Application application;

            public UserLoginInfo(String userName, String password, String appKey) {
                this.userName = userName;
                this.password = password;
                this.application = new Application(appKey);
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
