package com.example.skyapp.bo.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BO_response {

    public static class ProfileResponse {

        @SerializedName("data")
        private Data data;

        public String getFullName() {
            if (data != null &&
                    data.userData != null &&
                    !data.userData.isEmpty() &&
                    data.userData.get(0).userAuthenticationData != null &&
                    !data.userData.get(0).userAuthenticationData.isEmpty()) {
                return data.userData.get(0).userAuthenticationData.get(0).fullName;
            }
            return null;
        }

        public String getEmail() {
            if (data != null &&
                    data.userData != null &&
                    !data.userData.isEmpty() &&
                    data.userData.get(0).userAuthenticationData != null &&
                    !data.userData.get(0).userAuthenticationData.isEmpty()) {
                return data.userData.get(0).userAuthenticationData.get(0).email;
            }
            return null;
        }

        public String getUserUuid() {
            if (data != null &&
                    data.userData != null &&
                    !data.userData.isEmpty() &&
                    data.userData.get(0).userAuthenticationData != null &&
                    !data.userData.get(0).userAuthenticationData.isEmpty()) {
                return data.userData.get(0).userAuthenticationData.get(0).usrAuthenticationUuid;
            }
            return null;
        }

        public String getDateCreated() {
            if (data != null &&
                    data.userData != null &&
                    !data.userData.isEmpty() &&
                    data.userData.get(0).userAuthenticationData != null &&
                    !data.userData.get(0).userAuthenticationData.isEmpty()) {
                return data.userData.get(0).userAuthenticationData.get(0).dateCreated;
            }
            return null;
        }

        // === Internas ===
        private static class Data {
            @SerializedName("userData")
            private List<UserData> userData;
        }

        private static class UserData {
            @SerializedName("userType")
            private List<Object> userType;

            @SerializedName("userAuthenticationData")
            private List<UserAuthenticationData> userAuthenticationData;
        }

        private static class UserAuthenticationData {
            @SerializedName("usrAuthenticationId")
            private int usrAuthenticationId;

            @SerializedName("usrUserId")
            private int usrUserId;

            @SerializedName("usrAuthenticationTypeId")
            private int usrAuthenticationTypeId;

            @SerializedName("usrAuthenticationUuid")
            private String usrAuthenticationUuid;

            @SerializedName("email")
            private String email;

            @SerializedName("identifier")
            private String identifier;

            @SerializedName("dateCreated")
            private String dateCreated;

            @SerializedName("dateUpdated")
            private String dateUpdated;

            @SerializedName("description")
            private String description;

            @SerializedName("usrAuthenticationDataId")
            private int usrAuthenticationDataId;

            @SerializedName("firstName")
            private String firstName;

            @SerializedName("middleName")
            private String middleName;

            @SerializedName("lastName")
            private String lastName;

            @SerializedName("fullName")
            private String fullName;
        }
    }
}
