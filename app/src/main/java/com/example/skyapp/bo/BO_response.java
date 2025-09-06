package com.example.skyapp.bo;

import com.google.gson.annotations.SerializedName;

public class BO_response {
    public static class LoginResponse {

        @SerializedName("data")
        private Data data;

        public String getAccessToken() {
            return data != null && data.userLogged != null && data.userLogged.userToken != null
                    ? data.userLogged.userToken.appToken
                    : null;
        }

        public String getRefreshToken() {
            return data != null && data.userLogged != null && data.userLogged.refreshToken != null
                    ? data.userLogged.refreshToken.appToken
                    : null;
        }

        public int getUserId() {
            return data != null && data.userLogged != null && data.userLogged.user != null
                    ? data.userLogged.user.userId
                    : -1; // Retorna -1 si no se encuentra el userId
        }


        public String getUserUuid() {
            return data != null && data.userLogged != null && data.userLogged.user != null
                    ? data.userLogged.user.userUuid
                    : null; // Retorna null si no se encuentra el userUuid
        }


        private static class Data {
            @SerializedName("userLogged")
            private UserLogged userLogged;
        }

        private static class UserLogged {
            @SerializedName("user")
            private User user;
            @SerializedName("userToken")
            private UserToken userToken;

            @SerializedName("refreshToken")
            private RefreshToken refreshToken;
        }

        private static class UserToken {
            @SerializedName("appToken")
            private String appToken;
        }

        private static class RefreshToken {
            @SerializedName("appToken")
            private String appToken;
        }
        private static class User {
            @SerializedName("userId")
            private int userId;

            @SerializedName("userUuid")
            private String userUuid;
        }
    }

}
