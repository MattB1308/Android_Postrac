package com.example.skyapp.api_config.routing;

import com.example.skyapp.bo.routing.BO_request;
import com.example.skyapp.bo.routing.BO_response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RoutingInterface {

    /**
     * Get delivery routes by user and date
     * API: /api/routing/getDeliveryRouteByUserAndDate
     * @param bearerToken Authorization header with bearer token
     * @param request Request containing user info, userId, and date
     * @return Response containing list of routeIds
     */
    @POST("/api/routing/getDeliveryRouteByUserAndDate")
    Call<BO_response.DeliveryRouteResponse> getDeliveryRouteByUserAndDate(
            @Header("Authorization") String bearerToken,
            @Body BO_request.DeliveryRouteRequest request
    );

    /**
     * Get detailed route information by routeId
     * API: /api/routing/getDeliveryRouteInfoByRouteId
     * @param bearerToken Authorization header with bearer token
     * @param request Request containing user info and routeId
     * @return Response containing detailed route and package information
     */
    @POST("/api/routing/getDeliveryRouteInfoByRouteId")
    Call<BO_response.RouteDetailsResponse> getDeliveryRouteInfoByRouteId(
            @Header("Authorization") String bearerToken,
            @Body BO_request.RouteDetailsRequest request
    );
}