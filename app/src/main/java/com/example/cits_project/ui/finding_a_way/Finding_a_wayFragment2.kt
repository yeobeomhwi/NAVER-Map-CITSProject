package com.example.cits_project.ui.finding_a_way

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentFindingAWay2Binding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

// Finding_a_wayFragment2.kt

class Finding_a_wayFragment2 : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentFindingAWay2Binding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var marker: Marker
    private lateinit var infoWindow: InfoWindow

    private var locationSource: FusedLocationSource? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindingAWay2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mapFragment.getMapAsync(this)

        val startLocation = arguments?.getString("start_location")
        val endLocation = arguments?.getString("end_location")

//        searchAddress(startLocation, endLocation)

        // startLocation과 endLocation을 사용하여 동작을 수행합니다.
        // 예를 들어, TextView에 출발지와 도착지 정보를 표시할 수 있습니다.
        val textView = binding.textView

        val message = "출발지: $startLocation\n도착지: $endLocation"
        textView.text = message

        return root
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 출발지와 도착지 주소를 받아옵니다.
        val startLocation = arguments?.getString("start_location")
        val endLocation = arguments?.getString("end_location")

        // 주소를 좌표로 변환하고, 해당 좌표에 마커를 생성하여 지도에 추가합니다.
        searchAddress(startLocation, endLocation)
    }

    fun searchAddress(startAddress: String?, endAddress: String?) {
        val geocoder = Geocoder(requireContext())

        // 출발지 주소를 좌표로 변환하고 결과를 리스트에 저장합니다.
        val startList: List<Address>? = startAddress?.let { geocoder.getFromLocationName(it, 1) }

        // 도착지 주소를 좌표로 변환하고 결과를 리스트에 저장합니다.
        val endList: List<Address>? = endAddress?.let { geocoder.getFromLocationName(it, 1) }

        if (startList != null && startList.isNotEmpty() && endList != null && endList.isNotEmpty()) {
            val startLocation = LatLng(startList[0].latitude, startList[0].longitude)
            val endLocation = LatLng(endList[0].latitude, endList[0].longitude)

            // 출발지와 도착지의 중간 지점을 계산합니다.
            val middleLocation = LatLng(
                (startLocation.latitude + endLocation.latitude) / 2,
                (startLocation.longitude + endLocation.longitude) / 2
            )

            // 출발지와 도착지의 중간 지점으로 카메라를 이동시킵니다.
            val cameraUpdate = CameraUpdate.fitBounds(LatLngBounds.from(startLocation, endLocation), 100) // 100은 여백입니다.
            naverMap.moveCamera(cameraUpdate)

            // 출발지와 도착지의 마커를 생성하여 지도에 추가합니다.
            val startMarker = Marker()
            startMarker.position = startLocation
            startMarker.map = naverMap

            val endMarker = Marker()
            endMarker.position = endLocation
            endMarker.map = naverMap
        }
    }

}