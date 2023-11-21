package com.example.cits_project.Ulsan.IdService

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CITSIdService {
    @GET("citsapi/service/baseInfo")
    fun getCITSIdService(
        @Query("serviceKey") serviceKey: String,
        @Query("version") version: String
    ): Call<CITSIdResponse>
}
