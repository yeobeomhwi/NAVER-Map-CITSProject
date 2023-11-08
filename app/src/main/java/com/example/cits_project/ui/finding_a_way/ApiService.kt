package com.example.cits_project.ui.finding_a_way

// ApiService.kt
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("map-direction/v1/driving")
    fun getDirections(
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("option") option: String,
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String
    ): Call<DirectionsResponse>
}