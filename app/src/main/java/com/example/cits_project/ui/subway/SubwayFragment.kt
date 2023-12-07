package com.example.cits_project.ui.subway

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cits_project.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SubwayFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_subway, container, false)
        mapView = rootView.findViewById(R.id.nmap)
        mapView.onCreate(savedInstanceState)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.getMapAsync { naverMap ->
            this.naverMap = naverMap
            displaySubwayStationsOnMap()

            CoroutineScope(Dispatchers.Main).launch {
                fetchAndDisplayRealtimeTrainInfo("0222")
            }
        }
    }

    private fun displaySubwayStationsOnMap() {
        // 서울시 지하철 역 정보 (예시)
        val subwayStations = listOf(
            SubwayStation("StationA", LatLng(37.123, 127.456)),
            // ...
        )

        // 지하철 역 마커 표시
        subwayStations.forEach { station ->
            val marker = Marker()
            marker.position = station.latLng
            marker.map = naverMap
        }
    }

    private suspend fun fetchAndDisplayRealtimeTrainInfo(stationCode: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://openapi.seoul.go.kr:8088/466967786a73656f3336574f6f5951/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val subwayApi = retrofit.create(SubwayApi::class.java)

        try {
            val response = withContext(Dispatchers.IO){
                    subwayApi.getRealtimeArrivalInfo(stationCode, "466967786a73656f3336574f6f5951")
                }

                    // 예시: 응답 데이터를 기반으로 지도에 표시
                    response.realtimeArrivalList.forEach { arrivalInfo ->
                        val marker = Marker()
                        marker.position = LatLng(37.123, 127.456)  // 예시 위치, 실제 위치 설정 필요
                        marker.captionText = "${arrivalInfo.trainLineNm} - ${arrivalInfo.arvlMsg2}"
                        marker.map = naverMap

                }
            } catch (e: Exception) {
                // Handle exception (e.g., network error)
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}

data class SubwayStation(val name: String, val latLng: LatLng)

