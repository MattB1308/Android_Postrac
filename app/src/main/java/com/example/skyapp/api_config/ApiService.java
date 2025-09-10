package com.example.skyapp.api_config;

public enum ApiService {
    LOGIN("https://login.sepex.io/"),
    USERS("https://users.sepex.io/"),
    ROUTES("https://routes.sepex.io/"),
    SHIPMENTS("https://shipments.sepex.io/"),
    CHECKPOINTS("https://checkpoints.sepex.io/");

    private final String baseUrl;

    ApiService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}