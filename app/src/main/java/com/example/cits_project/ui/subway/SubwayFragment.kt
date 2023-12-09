package com.example.cits_project.ui.subway

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentSubwayBinding

class SubwayFragment : Fragment() {
    private var _binding: FragmentSubwayBinding? = null
    private val binding get() = _binding!!
    /*
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    */   override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubwayBinding.inflate(inflater, container, false)
        val root: View = binding.root
        /* mapView = root.findViewById(R.id.nmap)
         mapView.onCreate(savedInstanceState)*/
        // Find the ImageButton by its ID
        val subwayViewButton: ImageButton = root.findViewById(R.id.subwayView)

        // Set an OnClickListener for the ImageButton
        subwayViewButton.setOnClickListener {
            // Define the URL of the website
            val websiteUrl = "https://smss.seoulmetro.co.kr/traininfo/traininfoUserView.do"

            // Create an Intent to open a browser with the specified URL
            val urlintent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))

            // Check if there's a browser app to handle the intent before starting it
            if (urlintent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(urlintent)
            }
        }
        return root
    }
/*
    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
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
    }*/
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
}

/*
data class SubwayStation(val name: String, val latLng: LatLng)
*/
