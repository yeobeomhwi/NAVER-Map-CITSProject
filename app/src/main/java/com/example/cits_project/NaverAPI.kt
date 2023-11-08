package com.example.cits_project


import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Query
import com.example.cits_project.ResultPath
import retrofit2.http.Header

interface NaverAPI{
    @GET("your_path_here")
    fun getPath(
        @Header("X-NCP-APIGW-API-KEY-ID") apiKeyID: String,
        @Header("X-NCP-APIGW-API-KEY") apiKey: String,
        @Query("start") start: String,
        @Query("goal") goal: String
    ): Call<ResultPath>

}
