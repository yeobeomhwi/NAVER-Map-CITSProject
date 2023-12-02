package com.example.api_test_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.api_test_project.ui.theme.API_TEST_PROJECTTheme

// API
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import android.widget.Toast
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query
import androidx.compose.material3.Button
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


data class LatLngResponse(
    val x:String? = null,
    val y:String? = null,
    val traffic:String? = null
    // 기타 필요한 필드
)




interface ApiService {
    @POST("/get_lat_lng")
    fun getLatLng(
        @Query("x") x: Double,
        @Query("y") y: Double
    ): Call<LatLngResponse> // 네트워크 요청의 반환 타입을 명시해줍니다.
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.30.1.12:6000/") // 기본 URL 설정
            .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 연결 대기 시간 조정
                .readTimeout(30, TimeUnit.SECONDS) // 읽기 대기 시간 조정
                .writeTimeout(30, TimeUnit.SECONDS) // 쓰기 대기 시간 조정
                .build()
            )
            .addConverterFactory(GsonConverterFactory.create()) // Gson 컨버터 팩토리 추가
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        setContent {
            API_TEST_PROJECTTheme {
                val resultText = remember { mutableStateOf("No data yet") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(
                        onClick = {
                            // When the button is clicked, trigger API call
                            val call = apiService.getLatLng(37.5553, 126.93606)
                            call.enqueue(object : retrofit2.Callback<LatLngResponse> {
                                override fun onResponse(
                                    call: Call<LatLngResponse>,
                                    response: retrofit2.Response<LatLngResponse>
                                ) {
                                    if(response.isSuccessful){
                                        val data = response.body()
                                        data?.let {
                                            val xValue = it.x ?: "N/A"
                                            val yValue = it.y ?: "N/A"
                                            val trafficValue = it.traffic ?: "N/A"

                                            val logMessage = "x=$xValue, y=$yValue, traffic=$trafficValue"
                                            Log.d("ReceivedData", logMessage)
                                            runOnUiThread {
                                                Toast.makeText(this@MainActivity, logMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    Log.d("log", response.toString())
                                    Log.d("log", response.body().toString())
                                }

                                override fun onFailure(call: Call<LatLngResponse>, t: Throwable) {
                                    // Handle failure
                                    resultText.value = "API call failed: ${t.message}"
                                }
                            })
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(text = "Make API Call")
                    }

                    Text(
                        text = resultText.value,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    API_TEST_PROJECTTheme {
        Greeting("Android")
    }
}