package com.example.cits_project.ui.subway

import retrofit2.http.GET
import retrofit2.http.Query

interface SubwayApi {
    @GET("realtimeStationArrival")
    suspend fun getRealtimeArrivalInfo(
        @Query("stationId") stationId: String,
        @Query("apiKey") apiKey: String
    ): RealtimeArrivalResponse
}

data class RealtimeArrivalResponse(val realtimeArrivalList: List<RealtimeArrivalInfo>)

data class RealtimeArrivalInfo(
    val trainLineNm: String,
    val subwayHeading: String,
    val arvlMsg2: String,
    // Add other relevant fields as needed
)
