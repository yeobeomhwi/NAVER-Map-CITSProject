package com.example.cits_project.Ulsan.LocationService

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CITSLocationService {

    @GET("citsapi/service/tim/vms")
    fun getCITSLocationData(
        @Query("serviceKey") serviceKey: String,
        @Query("linkId") linkId: String
    ): Call<CITSLocationResponse>
}
