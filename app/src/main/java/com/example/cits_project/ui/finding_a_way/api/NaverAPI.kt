package com.example.cits_project.ui.finding_a_way.api


import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Query
import retrofit2.http.Header

interface NaverAPI{
    @GET("v1/driving")
    fun getPath(
        @Header("X-NCP-APIGW-API-KEY-ID") apiKeyID: String,
        @Header("X-NCP-APIGW-API-KEY") apiKey: String,
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("option") option: String // 옵션 코드를 추가합니다.
    ): Call<ResultPath>

}
