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

class Finding_a_wayFragment2 : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentFindingAWay2Binding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var marker: Marker
    private lateinit var infoWindow: InfoWindow

    private var locationSource: FusedLocationSource? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindingAWay2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        // FusedLocationSource 초기화
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mapFragment.getMapAsync(this)

        val startLocation = arguments?.getString("start_location")
        val endLocation = arguments?.getString("end_location")

        // startLocation과 endLocation을 사용하여 동작을 수행합니다.
        // 예를 들어, TextView에 출발지와 도착지 정보를 표시할 수 있습니다.
        val textView = binding.textView

        val message = "출발지: $startLocation\n도착지: $endLocation"
        textView.text = message

        return root
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        naverMap.locationSource = locationSource

        naverMap.locationTrackingMode = LocationTrackingMode.Face


        val startLocation = arguments?.getString("start_location")
        val endLocation = arguments?.getString("end_location")

        searchAddress(startLocation, endLocation)

        naverMap.apply {
            mapType = NaverMap.MapType.Navi

            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)

            buildingHeight = 0.5f
            symbolPerspectiveRatio = 1f

            val cameraPosition = CameraPosition(
                LatLng(0.0, 0.0),
                18.0,
                70.0,
                0.0
            )

            uiSettings.apply {
                isLocationButtonEnabled = true
                isTiltGesturesEnabled = true
                isRotateGesturesEnabled = true
                isCompassEnabled = false
                isZoomControlEnabled = true
            }
        }
    }

    fun searchAddress(startAddress: String?, endAddress: String?) {
        val geocoder = Geocoder(requireContext())

        val startList: List<Address>? = startAddress?.let { geocoder.getFromLocationName(it, 1) }
        val endList: List<Address>? = endAddress?.let { geocoder.getFromLocationName(it, 1) }

        if (startList != null && startList.isNotEmpty() && endList != null && endList.isNotEmpty()) {
            val startLocation = LatLng(startList[0].latitude, startList[0].longitude)
            val endLocation = LatLng(endList[0].latitude, endList[0].longitude)

            val middleLocation = LatLng(
                (startLocation.latitude + endLocation.latitude) / 2,
                (startLocation.longitude + endLocation.longitude) / 2
            )

            val cameraUpdate = CameraUpdate.fitBounds(LatLngBounds.from(startLocation, endLocation), 100)
            naverMap.moveCamera(cameraUpdate)

            val startMarker = Marker()
            startMarker.position = startLocation
            startMarker.map = naverMap

            val endMarker = Marker()
            endMarker.position = endLocation
            endMarker.map = naverMap

            // 출발지와 도착지의 좌표를 로그로 출력합니다.
            val startLatLng = "출발지 좌표: ${startLocation.latitude}, ${startLocation.longitude}"
            val endLatLng = "도착지 좌표: ${endLocation.latitude}, ${endLocation.longitude}"
            println(startLatLng)
            println(endLatLng)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
