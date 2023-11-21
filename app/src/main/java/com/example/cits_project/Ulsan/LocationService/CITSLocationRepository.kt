package com.example.cits_project.Ulsan.LocationService

import android.util.Log
import com.example.cits_project.Ulsan.IdService.CITSIdRepository
import com.example.cits_project.Ulsan.IdService.CITSIdService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.Provider.Service

// Retrofit을 사용하여 CITS API와 통신하는 Repository 클래스
class CITSLocationRepository {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(CITSLocationInterceptor())
        .build()

    // Moshi 인스턴스 생성
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Retrofit 인스턴스 생성
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://apis.data.go.kr/6310000/") // CITS API의 base URL
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    // Retrofit을 통해 사용할 Service 인터페이스 생성
    private val service = retrofit.create(CITSLocationService::class.java)

    // CITS API로부터 위치 정보를 가져오는 함수
    fun getCITSLocationService(
        serviceKey: String,
        linkId: String,
        onSuccess: (CITSLocationResponse?) -> Unit,
        onError: (String) -> Unit
    ) {
        // Retrofit을 사용한 비동기적인 호출
        val call = service.getCITSLocationData(serviceKey, linkId)
        call.enqueue(object : Callback<CITSLocationResponse> {
            override fun onResponse(
                call: Call<CITSLocationResponse>,
                response: Response<CITSLocationResponse>
            ) {
                if (response.isSuccessful) {
                    onSuccess(response.body())
                } else {
                    onError("Location 에러: ${response.code()}")
                    val errorBody = response.errorBody()?.string()
                    Log.e("CITSLocationResponse", "Error Body: $errorBody")
                }
            }

            override fun onFailure(call: Call<CITSLocationResponse>, t: Throwable) {
                onError("Location 요청 실패: ${t.message}")
            }
        })
    }

    // CITS API를 호출하기 위한 Interceptor 구현
    class CITSLocationInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            // CITS API에 필요한 헤더 추가 등의 작업 수행
            val newRequest = chain.request().newBuilder()
                .addHeader("Your-CITS-Header", "Your-CITS-Header-Value")
                .build()
            return chain.proceed(newRequest)
        }
    }
}


