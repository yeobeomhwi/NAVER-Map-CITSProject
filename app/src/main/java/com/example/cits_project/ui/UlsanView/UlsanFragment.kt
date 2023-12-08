    // 패키지 및 라이브러리 임포트
    package com.example.cits_project.ui.UlsanView

    import android.Manifest
    import android.content.pm.PackageManager
    import android.graphics.Color
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
    import com.google.firebase.database.ValueEventListener
    import com.google.firebase.database.ktx.database
    import com.google.firebase.ktx.Firebase
    import com.naver.maps.geometry.LatLng
    import com.naver.maps.map.*
    import com.naver.maps.map.overlay.InfoWindow
    import com.naver.maps.map.overlay.Marker
    import com.naver.maps.map.util.FusedLocationSource
    import com.naver.maps.map.widget.ZoomControlView
    import okhttp3.OkHttpClient
    import okhttp3.Request
    import org.json.JSONObject
    import com.naver.maps.map.util.MarkerIcons
    import okhttp3.Call
    import okhttp3.Callback
    import okhttp3.Response
    import org.json.JSONException
    import java.io.IOException

    // UlsanFragment 클래스 선언, Fragment 및 OnMapReadyCallback 구현
    class UlsanFragment : Fragment(), OnMapReadyCallback {

        // FragmentUlsanBinding 인스턴스 생성
        private var _binding: FragmentUlsanBinding? = null
        private val binding get() = _binding!!

        // 위치 소스 및 NaverMap 인스턴스 생성
        private lateinit var locationSource: FusedLocationSource
        private lateinit var naverMap: NaverMap
        private lateinit var infoWindow: InfoWindow

        // 마커 관리를 위한 리스트 및 Firebase 데이터베이스 참조
        private val markers = mutableListOf<Marker>()
        private val database = Firebase.database.reference
        private val dataRef = database.child("")


        // 위치 권한 요청 코드 및 권한 배열 정의
        companion object {
            const val LOCATION_PERMISSION_REQUEST_CODE = 1000
            val PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        // onCreateView: Fragment의 뷰를 생성하고 위치 권한 체크 후 지도 초기화 또는 권한 요청
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

        // 데이터 모델 클래스 정의
        data class Items(
            val lgtd: Double?,
            val linkId: String?,
            val lttd: Double?
        )

        // 신호 정보 데이터 모델 클래스 정의
        data class SignalInfo(
            val direction: Int,
            val type: Int,
            val color: Int,
            val remainTime: Int
        )

        // initMap: 지도 초기화 함수
        private fun initMap() {
            val mapFragment = childFragmentManager.findFragmentById(R.id.nmap) as MapFragment?
                ?: MapFragment.newInstance().also {
                    childFragmentManager.beginTransaction().add(R.id.nmap, it).commit()
                }
            mapFragment.getMapAsync(this)
            locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        }

        // checkLocationPermission: 위치 권한 체크 함수
        private fun checkLocationPermission(): Boolean {
            return PERMISSIONS.all {
                ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
            }
        }

        // requestLocationPermission: 위치 권한 요청 함수
        private fun requestLocationPermission() {
            ActivityCompat.requestPermissions(
                requireActivity(),
                PERMISSIONS,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // onRequestPermissionsResult: 위치 권한 결과 처리 함수
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

        // onMapReady: 지도 준비 완료 시 호출되는 콜백 함수
        override fun onMapReady(naverMap: NaverMap) {
            this.naverMap = naverMap
            naverMap.locationSource = locationSource

            naverMap.apply {
                // 지도 설정
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

                // 현재 위치 변경 시 이벤트 처리
                naverMap.addOnLocationChangeListener { location ->
                    if (location != null) {
                        val currentLttd = location.latitude
                        val currentLgtd = location.longitude

                        // fetchData 함수 호출
                        fetchData(naverMap, currentLttd, currentLgtd)

                        // 현재위치 좌표 출력
                        val currentLatLng = "현재위치 좌표: $currentLttd, $currentLgtd"
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

        private fun fetchData(naverMap: NaverMap, currentLttd: Double, currentLgtd: Double) {
            val uniqueLinkIds = HashSet<String>() // 중복된 linkId를 방지하기 위한 Set

            database.child("Ulsan").child("Location-info").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val lttd = dataSnapshot.child("integBody").child("items").child("0").child("lttd").getValue(Double::class.java)
                        val lgtd = dataSnapshot.child("integBody").child("items").child("0").child("lgtd").getValue(Double::class.java)
                        val linkId = dataSnapshot.child("integBody").child("items").child("0").child("link_id").getValue(String::class.java)

                        if (lttd != null && lgtd != null && linkId != null) {
                            val distance = calculateDistance(currentLttd, currentLgtd, lttd, lgtd)
                            if (distance <= 0.3) {
                                uniqueLinkIds.add(linkId)

                                fetchSignalInfo(linkId, lttd, lgtd, naverMap)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchData", "데이터 로드 취소: $error")
                }
            })
        }





        // calculateDistance: 두 지점 간의 거리 계산 함수
        private fun calculateDistance(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val R = 6371 // 지구 반지름 (km)
            val dLat = deg2rad(lat2 - lat1)
            val dLon = deg2rad(lon2 - lon1)
            val a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(
                    deg2rad(lat2)
                ) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return R * c // 거리 (km)
        }

        // deg2rad: 각도를 라디안으로 변환하는 함수
        private fun deg2rad(deg: Double): Double {
            return deg * (Math.PI / 180)
        }

        // onDestroyView: Fragment의 뷰가 소멸될 때 호출되는 함수
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        private fun fetchSignalInfo(linkId: String, lttd: Double, lgtd: Double, naverMap: NaverMap) {
            val serviceKey = "TOlfl5zsDX0idc1uqdtoVkQkk7oSlUV%2BMqks%2FOYbEuYjRtgy8j%2B4Vv4rrFOFQm9YHCIOlPr91KwSNqe0yJrSEg%3D%3D"

            // API 요청을 위한 URL 생성
            val signalApiUrl = "https://apis.data.go.kr/6310000/citsapi/service/signal?serviceKey=$serviceKey&linkId=$linkId"

            // OkHttpClient 객체를 생성
            val client = OkHttpClient()

            // API 요청에 필요한 Request 객체를 생성
            val request = Request.Builder().url(signalApiUrl).build()

            // 비동기적으로 API 요청을 수행하고 응답을 처리하는 부분
            client.newCall(request).enqueue(object : Callback {
                // 성공적인 응답을 받았을 때 호출되는 메서드
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        // 응답에서 데이터를 추출
                        val responseBody = response.body?.string()

                        // 응답 데이터가 비어있지 않은 경우
                        if (!responseBody.isNullOrEmpty()) {
                            // JSON 데이터를 파싱하여 SignalInfo 객체로 변환
                            val signalInfo = parseSignalInfo(responseBody)

                            // 변환된 SignalInfo 객체를 이용하여 마커를 표시하는 함수 호출
                            if (signalInfo.type == 1) {
                                showMarkerWithSignalInfo(signalInfo, lttd, lgtd)
                            }
                        }
                    }
                }

                // API 요청이 실패했을 때 호출되는 메서드
                override fun onFailure(call: Call, e: IOException) {
                    // 오류 로그 출력
                    Log.e("FetchSignal", "Failed to fetch signal data: $e")
                }
            })
        }



        private fun parseSignalInfo(responseBody: String): SignalInfo {
            try {
                val json = JSONObject(responseBody)
                val itemsArray = json.getJSONObject("body").getJSONArray("items")

                if (itemsArray.length() > 0) {
                    val firstItem = itemsArray.getJSONObject(0)
                    val realtimeArray = firstItem.getJSONArray("realtime")

                    if (realtimeArray.length() > 0) {
                        val firstRealtime = realtimeArray.getJSONObject(0)

                        val direction = firstRealtime.getInt("dir")
                        val type = firstRealtime.getInt("type")
                        val color = firstRealtime.getInt("color")
                        val remainTime = firstRealtime.getInt("remain_time")

                        return SignalInfo(direction, type, color, remainTime)
                    }
                }
            } catch (e: JSONException) {
                // 추가로 예외 정보 출력
                e.printStackTrace()
            }

            // 기본값 반환 또는 예외 처리를 수행할 수 있습니다.
            return SignalInfo(0, 0, 0, 0)
        }





        // showMarkerWithSignalInfo 함수 내의 수정된 부분
        private fun showMarkerWithSignalInfo(signalInfo: SignalInfo, lttd: Double, lgtd: Double) {
            // 신호 정보에 따라 마커의 색상을 결정
            val color = when (signalInfo.color) {
                1 -> Color.RED    // Red
                2 -> Color.YELLOW // Orange
                3 -> Color.GREEN  // Green
                else -> Color.BLACK // Default (Black)
            }

            // Activity가 null이 아닌 경우에 UI 업데이트를 수행
            activity?.runOnUiThread {
                // Marker 객체 생성 및 설정
                val marker = Marker(LatLng(lttd, lgtd)).apply {
                    map = naverMap
                    captionText = "${signalInfo.remainTime} 초"

                    // Marker 아이콘 설정
                    icon = MarkerIcons.BLACK
                    // Marker 아이콘 색상 설정
                    iconTintColor = color
                }

                // 마커를 리스트에 추가하여 추적
                markers += marker
            }
        }


    }
