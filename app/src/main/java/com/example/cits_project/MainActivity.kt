package com.example.cits_project

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cits_project.Weather.OpenWeatherMapService
import com.example.cits_project.Weather.WeatherResponse
import com.example.cits_project.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // 날씨 변수
    private val API_KEY = "af8b16741ab74e53544c95208262593b"  // OpenWeatherMap에서 발급받은 API 키
    private val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private val iconBaseUrl = "https://openweathermap.org/img/w/"
    // 뷰 바인딩을 위한 변수
    private lateinit var binding: ActivityMainBinding
    // 앱 바 구성을 위한 변수
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var naverMap: NaverMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 네이버 지도 초기화
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정
        setSupportActionBar(binding.appBarMain.toolbar)

        // 네비게이션 뷰와 네비게이션 컨트롤러 초기화
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // 앱 바 구성 초기화
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_finding_a_way, R.id.nav_slideshow
            ), drawerLayout
        )

        // 네비게이션 뷰와 앱 바 컨트롤러 설정
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //날씨 뷰
        val temperatureTextView = findViewById<TextView>(R.id.temperatureTextView)
        val weatherIconView = findViewById<ImageView>(R.id.weatherIconView)
        val themeButton = findViewById<Button>(R.id.themeButton)

        themeButton.setOnClickListener {
            // 버튼을 클릭하면 지도 스타일 변경
            changeMapStyle()
        }
        val location = "Seoul,KR"  // 날씨 검색 위치
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val service = retrofit.create(OpenWeatherMapService::class.java)

        val call = service.getWeather(location, API_KEY)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    Log.d("weather", "Success: $weatherData")
                    val temperatureKelvin = weatherData?.main?.temp
                    val temperatureCelsius = temperatureKelvin?.minus(273.15)?.roundToInt()
                    val iconCode = weatherData?.weather?.getOrNull(0)?.icon
                    val iconUrl = "$iconBaseUrl$iconCode.png"

                    runOnUiThread {
                        temperatureTextView.text = "$temperatureCelsius°C"

                        Glide.with(this@MainActivity)
                            .load(iconUrl)
                            .into(weatherIconView)
                        Log.d("weather", "Icon URL: $iconUrl")
                    }
                } else {
                    Log.e("weather", "Error: ${response.code()} - ${response.message()}")
                    // API 호출은 성공했지만 응답이 실패한 경우 처리
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // 네트워크 오류 등의 실패한 경우 처리
                Log.e("weather", "Failure: ${t.message}")
            }
            // 버튼 클릭 이벤트 처리

        })

    }
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 초기 위치 설정 (서울의 좌표)
        val target = LatLng(37.5665, 126.9780)
        val cameraUpdate = CameraUpdate.scrollTo(target)
        naverMap.moveCamera(cameraUpdate)

        // 지도 스타일 설정
        val mapType = NaverMap.MapType.Navi
        naverMap.mapType = mapType // 일반 지도

        // 테마 기능 활성화
        naverMap.setNightModeEnabled(false)
    }
    private fun changeMapStyle() {
        // 버튼을 클릭하면 지도 스타일 변경
        val currentMapType = naverMap.mapType
        val newMapType = when (currentMapType) {
            NaverMap.MapType.Navi -> NaverMap.MapType.Basic
            NaverMap.MapType.Basic -> NaverMap.MapType.Satellite
            NaverMap.MapType.Satellite -> NaverMap.MapType.Hybrid
            NaverMap.MapType.Hybrid -> NaverMap.MapType.Terrain
            NaverMap.MapType.Terrain -> NaverMap.MapType.Navi
            else -> NaverMap.MapType.Navi
        }
        naverMap.mapType = newMapType
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 메뉴를 인플레이트하여 액션 바에 추가
        menuInflater.inflate(R.menu.main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        // 네비게이션 컨트롤러를 이용하여 뒤로 가기 동작 처리
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
