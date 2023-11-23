package com.example.cits_project.ui.UlsanView

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentUlsanBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.ZoomControlView

class UlsanFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentUlsanBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var infoWindow: InfoWindow

    private var markers = emptyList<Marker>()
    // Firebase Database 참조를 위한 변수
    private val database = Firebase.database.reference
    private val dataRef = database.child("")



    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUlsanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (checkLocationPermission()) {
            initMap()
        } else {
            requestLocationPermission()
        }

        return root
    }
    data class Items(
        val lgtd: Double?,
        val linkId: String?,
        val lttd: Double?
    )

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.nmap) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.nmap, it).commit()
            }
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun checkLocationPermission(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (checkLocationPermission()) {
                initMap()
            } else {
                Toast.makeText(
                    requireContext(),
                    "위치 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        naverMap.apply {
            mapType = NaverMap.MapType.Navi
            naverMap.locationTrackingMode = LocationTrackingMode.Face

            val zoomControlView = binding.root.findViewById(R.id.zoom) as ZoomControlView
            zoomControlView.map = naverMap

            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)

            buildingHeight = 0.5f
            symbolPerspectiveRatio = 1f

            val mode2DcameraPosition = CameraPosition(
                LatLng(35.556908, 129.277786),
                19.0,
                90.0,
                0.0
            )

            addOnLocationChangeListener { location ->
                if (location != null) {
                    val currentLttd = location.latitude
                    val currentLgtd = location.longitude

                    // fetchData 함수 호출
                    fetchData(naverMap, currentLttd, currentLgtd)
                    Log.d("!1","${fetchData(naverMap, currentLttd, currentLgtd)}")

                    // 현재위치 좌표 출력
                    val currentLatLng = "현재위치 좌표: $currentLttd, $currentLgtd"
                    println(currentLatLng)
                } else {
                    println("현재위치 좌표: Location unavailable")
                }
            }

            moveCamera(CameraUpdate.toCameraPosition(mode2DcameraPosition))

            minZoom = 8.0
            maxZoom = 20.0

            uiSettings.apply {
                isLocationButtonEnabled = true
                isTiltGesturesEnabled = true
                isRotateGesturesEnabled = true
                isCompassEnabled = false
                isZoomControlEnabled = false
            }

            val locationOverlay = locationOverlay
            locationOverlay.isVisible = true

        }

    }


// ...

    private fun fetchData(naverMap: NaverMap, currentLttd: Double, currentLgtd: Double) {
        for (i in 0 until 1410) {
            val ulsanLocationInfoRef = database.child("Ulsan").child("Location-info").child(i.toString())
            ulsanLocationInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    // 데이터를 가져와서 타입에 맞게 변환
                    val lttd = snapshot.child("lttd").getValue(Double::class.java)
                    val lgtd = snapshot.child("lgtd").getValue(Double::class.java)

                    // 3km 이내에 있는 데이터를 가져옴
                    if (lttd != null && lgtd != null &&
                        calculateDistance(currentLttd, currentLgtd, lttd, lgtd) <= 0.5
                    ) {
                        Marker(LatLng(lttd, lgtd)).apply {
                            map = naverMap
                            // 마커 속성을 사용자 정의하거나 여기에 추가 데이터를 추가할 수 있습니다.
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchData", "데이터 로드 취소: $error")
                }
            })
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Radius of the earth in km
        val dLat = deg2rad(lat2 - lat1)
        val dLon = deg2rad(lon2 - lon1)
        val a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(
                deg2rad(lat2)
            ) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c // Distance in km
    }

    private fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
