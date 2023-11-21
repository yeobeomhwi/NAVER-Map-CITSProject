package com.example.cits_project.Search

import android.content.res.Resources
import com.example.cits_project.R
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Query

// 검색 기능을 담당하는 Repository
object SearchRepository {

    // OkHttpClient 인스턴스 생성
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AppInterceptor())
        .build()

    // Moshi 인스턴스 생성
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Retrofit 인스턴스 생성
    val retrofit = Retrofit.Builder()
        .baseUrl("https://openapi.naver.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    // Retrofit을 통해 사용할 Service 인터페이스 생성
    private val Service = retrofit.create(SearchService::class.java)

    // 검색 결과를 가져오는 함수
    fun getSearchPoint(query: String): Call<SearchResult> {
        return Service.getSearchPoint(query= " $query ", display = 20)
    }

    // 네이버 Open API를 호출하기 위한 Interceptor 구현
    class AppInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            // 네이버 Open API에 필요한 클라이언트 ID와 Secret을 헤더에 추가
            val clientID = MyApplication.applicationContext.getString(R.string.naver_search_client_id)
            val clientSecret = MyApplication.applicationContext.getString(R.string.naver_search_client_secret)
            val newRequest = chain.request().newBuilder()
                .addHeader("X-Naver-Client-Id", clientID)
                .addHeader("X-Naver-Client-Secret", clientSecret)
                .build()
            return chain.proceed(newRequest)
        }
    }
}
