package com.example.cits_project.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentHomeBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class HomeFragment : Fragment(), OnMapReadyCallback {

    // View Binding을 사용하여 레이아웃과 상호작용하는 데 필요한 바인딩 변수 선언
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 위치 정보 및 지도에 대한 변수 초기화
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var marker: Marker
    private lateinit var infoWindow : InfoWindow

    // 위치 권한 요청에 필요한 코드와 권한 목록 정의
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // 뷰 생성 시 호출되는 함수
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // HomeViewModel 초기화
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // View Binding 초기화
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 위치 권한 확인 및 초기화 함수 호출
        if (checkLocationPermission()) {
            initMap()
        } else {
            requestLocationPermission()
        }

//        // 검색 버튼 클릭 이벤트 설정
//        val searchButton = root.findViewById<ImageButton>(R.id.search_button)
//        searchButton.setOnClickListener {
//            val searchText = root.findViewById<EditText>(R.id.search_edit_text).text.toString()
//            searchAddress(searchText)
//        }

        // 뷰 반환
        return root
    }

    // 지도 초기화 함수
    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    // 위치 권한 확인 함수
    private fun checkLocationPermission(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PERMISSION_GRANTED
        }
    }

    // 위치 권한 요청 함수
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // 위치 권한 요청 결과 처리 함수
    override fun onRequestPermissionsResult(
        requestCode: Int,  // 권한 요청 코드
        permissions: Array<String>,  // 요청한 권한 목록
        grantResults: IntArray  // 권한 요청 결과
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {  // 만약 요청 코드가 위치 권한 요청 코드와 일치한다면
            if (checkLocationPermission()) {  // 만약 위치 권한이 허용되었다면
                initMap()  // 지도 초기화 함수 호출
            } else {  // 만약 위치 권한이 거부되었다면
                Toast.makeText(
                    requireContext(),
                    "위치 권한이 필요합니다.",  // 위치 권한이 필요하다는 메시지를 토스트로 표시
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {  // 위치 권한 요청 코드가 일치하지 않는 경우
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)  // 기본 동작을 수행 (상위 클래스의 함수 호출)
        }
    }


    // 지도 준비 완료 시 호출되는 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource  // 위치 소스를 설정하여 위치 정보를 가져오도록 합니다.

        naverMap.locationTrackingMode = LocationTrackingMode.Face

        // 여기에 추가적인 지도 설정 및 기능을 작성하세요...

        // 마커 초기화
        marker = Marker()
        marker.width = 60
        marker.height = 80
        // InfoWindow 초기화
        infoWindow = InfoWindow()


        naverMap.apply {
            // 지도 유형 설정
            mapType = NaverMap.MapType.Navi // 네비게이션용 지도로 설정

            // 레이어 그룹 활성화 설정
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true) // 실시간 교통 정보 표시
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true) // 건물 표시
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true) // 대중교통 정보 표시

            buildingHeight = 0.5f // 건물 높이 50% 지정
            symbolPerspectiveRatio = 1f // 심볼 원근감

            // 카메라 포지션 설정
            val cameraPosition = CameraPosition(
                LatLng(0.0, 0.0),
                18.0, // 줌 레벨
                70.0, // 기울임 각도
                0.0 // 베어링 각도
            )
            //현재위치 좌표
//            naverMap.addOnLocationChangeListener { location ->
//                if (location != null) {
//                    Log.d("LocationInfo", "${location.latitude}, ${location.longitude}")
//                } else {
//                    Log.d("LocationInfo", "Location unavailable")
//                }
//            }
            naverMap.addOnLocationChangeListener { location ->
                if (location != null) {
                    val currentLatLng = "현재위치 좌표: ${location.latitude}, ${location.longitude}"
                    println(currentLatLng)
                } else {
                    println("현재위치 좌표: Location unavailable")
                }
            }
            // 카메라 적용
            moveCamera(CameraUpdate.toCameraPosition(cameraPosition))

            // 줌 레벨 설정
            minZoom = 15.0 // 최소 줌 레벨
            maxZoom = 20.0 // 최대 줌 레벨

            // UI 설정
            uiSettings.apply {
                isLocationButtonEnabled = true // 현재 위치 활성화
                isTiltGesturesEnabled = true // 틸트 활성화
                isRotateGesturesEnabled = true // 회전 활성
                isCompassEnabled = false // 나침반비활성화
                isZoomControlEnabled = true //줌 컨트롤바 활성화
            }

            // 현재 위치 아이콘을 보이게 합니다.
            val locationOverlay = locationOverlay
            locationOverlay.isVisible = true
//
//            ActivityCompat.requestPermissions(this@MapViewActivity, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
//            Toast.makeText(this@MapViewActivity, "맵 초기화 완료", Toast.LENGTH_LONG).show()

//          // 검색 버튼 클릭 시 호출될 함수를 설정합니다.
            val searchButton = binding.searchButton
//
//          // 검색창에서 텍스트를 가져와서 주소로 검색합니다
            binding.searchButton.setOnClickListener{
                val searchText = binding.searchEditText.text.toString()
                infoWindow.close()
                searchAddress(searchText)
            }
        }
    }

    // 주소 검색 함수
    fun searchAddress(address: String) {

        // Geocoder 클래스를 사용하여 주소를 좌표로 변환하는 함수

        // Geocoder 객체를 생성하고, 현재 액티비티(this)를 사용합니다.
        val geocoder = Geocoder(requireContext())

        // 입력된 주소를 좌표로 변환하고 결과를 리스트에 저장합니다.
        val list = geocoder.getFromLocationName(address, 1)

        // 결과 리스트가 null이 아닌지 확인합니다.
        if (list != null) {

            // 결과 리스트가 비어있지 않은지 확인합니다.
            if (list.isNotEmpty()) {
                // 주소를 좌표로 변환한 결과 리스트에서 첫 번째 항목을 가져와서 위치로 설정합니다.
                val location = LatLng(list[0].latitude, list[0].longitude)

                // 마커의 위치를 변환된 좌표로 설정합니다.
                marker.position = location

                // 마커 클릭 시 표시될 InfoWindow 생성
                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        // 마커를 클릭했을 때 표시할 주소 정보를 반환합니다.
                        return list[0].getAddressLine(0)
                    }
                }

                marker.setOnClickListener {
                    // 마커를 클릭했을 때 InfoWindow를 엽니다.
                    infoWindow.open(marker)
                    true
                }

                // 마커를 Naver 지도에 추가합니다.
                marker.map = naverMap
                // 마커가 표시된 위치로 카메라를 이동시킵니다.
                val cameraUpdate = CameraUpdate.scrollTo(location)
                naverMap.moveCamera(cameraUpdate)

            } else {
                // 변환된 좌표가 없을 경우, 사용자에게 메시지를 표시합니다.
                Toast.makeText(requireContext(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fragment가 뷰를 파기할 때 호출되는 함수
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
