package com.example.cits_project.Ulsan.IdService

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object CITSIdRepository {

    // OkHttpClient 인스턴스 생성
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(CITSIdInterceptor())
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
    private val service = retrofit.create(CITSIdService::class.java)

    // CITS API를 호출하는 함수
    fun getCITSIdService(serviceKey: String, version: String, onSuccess: (CITSIdResponse?) -> Unit, onError: (String) -> Unit) {
        val call = service.getCITSIdService(serviceKey, version)

        call.enqueue(object : Callback<CITSIdResponse> {
            override fun onResponse(call: Call<CITSIdResponse>, response: Response<CITSIdResponse>) {
                if (response.isSuccessful) {
                    val citsResponse = response.body()
                    onSuccess(citsResponse)
                } else {
                    onError("ID 에러: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CITSIdResponse>, t: Throwable) {
                onError("ID 요청 실패: ${t.message}")
            }
        })
    }


    // CITS API를 호출하기 위한 Interceptor 구현
    class CITSIdInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            // CITS API에 필요한 헤더 추가 등의 작업 수행
            val newRequest = chain.request().newBuilder()
                .addHeader("Your-CITS-Header", "Your-CITS-Header-Value")
                .build()
            return chain.proceed(newRequest)
        }
    }
}
