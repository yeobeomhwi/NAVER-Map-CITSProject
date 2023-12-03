package com.example.cits_project.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentHomeBinding
import com.example.cits_project.Search.SearchRepository
import com.example.cits_project.Search.SearchResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.internal.notify
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val recyclerViewAdapter = recyclerViewAdapter {
        //카메라 움직임
        collapseBottomSheet()
        moveCamera(it,17.0)

    }
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var infoWindow: InfoWindow

    private var markers = emptyList<Marker>()


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


    // Fragment 뷰를 생성할 때 호출되는 함수
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // View Binding 초기화
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 위치 권한 확인 및 초기화 함수 호출
        if (checkLocationPermission()) {
            initMap()
        } else {
            requestLocationPermission()
        }

        // fragment_home.xml에서 include된 View에 대한 참조 얻기
        val bottomSheetLayout = root.findViewById<ConstraintLayout>(R.id.bottom_Sheet_layout)

        // bottom_sheet.xml에서 RecyclerView에 대한 참조 얻기
        val recyclerView = bottomSheetLayout.findViewById<RecyclerView>(R.id.bottomsSheetRecyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
                adapter = recyclerViewAdapter
        }


// SearchView 초기화 및 검색 이벤트 처리
        val searchView = root.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return if (!query.isNullOrEmpty()) {
                    // Retrofit을 사용하여 검색 API 호출
                    performSearch(query)

                    // 키보드를 숨김
                    val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)
                    true
                } else {
                    false
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 텍스트가 변경될 때의 동작 (현재는 처리하지 않음)
                return true
            }
        })

        // 뷰 반환
        return root
    }


    private fun performSearch(query: String) {
        // Retrofit을 사용하여 검색 API 호출
        SearchRepository.getSearchPoint(query).enqueue(object : Callback<SearchResult> {
            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                // API 응답 성공 처리
                val searchItemList = response.body()?.items.orEmpty()

                if (searchItemList.isEmpty()) {
                    // 검색 결과가 없는 경우
                    Toast.makeText(context, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                // 기존 마커 제거
                markers.forEach {
                    it.map = null
                }

                // 새로운 검색 결과로 마커 생성
                markers = searchItemList.map {
                    val cleanTitle = it.getCleanTitle()
                    Marker(
                        LatLng(
                            it.mapy.toDouble() / 10000000.0,  // 위도
                            it.mapx.toDouble() / 10000000.0   // 경도
                        )
                    ).apply {
                        captionText = cleanTitle // <b> 태그가 한번씩 나와서 태그 제거
                        map = naverMap
                    }
                }

                recyclerViewAdapter.setData(searchItemList)
                collapseBottomSheet()
                moveCamera(markers.first().position, 13.0)

            }

            override fun onFailure(call: Call<SearchResult>, t: Throwable) {
                // API 호출 실패 처리
            }
        })
    }

//...


    private fun moveCamera(position: LatLng,zoomLevel: Double){
        // 첫 번째 검색 결과 위치로 카메라 이동
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(position, zoomLevel)
            .animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun collapseBottomSheet(){
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.root)
        bottomSheetBehavior.state = STATE_COLLAPSED
    }

    // 지도 초기화 함수
    private fun initMap() {
        // 지도 Fragment를 가져오거나 생성하여 추가
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
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
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

    // 지도 준비 완료 시 호출되는 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Face

        naverMap.apply {
            // 지도 유형 설정
            mapType = NaverMap.MapType.Navi

            // 레이어 그룹 활성화 설정
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)

            // 건물 높이와 심볼 원근감 설정
            buildingHeight = 0.5f
            symbolPerspectiveRatio = 1f

            // 카메라 초기 위치 설정
            val cameraPosition = CameraPosition(
                LatLng(0.0, 0.0),
                18.0, // 줌 레벨
                0.0, // 기울임 각도
                0.0  // 베어링 각도
            )

            // 현재 위치 좌표를 출력
            addOnLocationChangeListener { location ->
                if (location != null) {
                    val currentLatLng = "현재위치 좌표: ${location.latitude}, ${location.longitude}"
                    println(currentLatLng)
                } else {
                    println("현재위치 좌표: Location unavailable")
                }
            }

            // 카메라 이동 및 설정
            moveCamera(CameraUpdate.toCameraPosition(cameraPosition))

            // 최소 및 최대 줌 레벨 설정
            minZoom = 10.0
            maxZoom = 20.0

            // 현재 위치 아이콘을 보이게 설정
            val locationOverlay = locationOverlay
            locationOverlay.isVisible = true
            // UI 설정
            uiSettings.apply {
                isLocationButtonEnabled = true  // 현재 위치 버튼 활성화
                isTiltGesturesEnabled = true  // 틸트 제스처 활성화
                isRotateGesturesEnabled = true  // 회전 제스처 활성화
                isCompassEnabled = false  // 나침반 비활성화
                isZoomControlEnabled = true  // 줌 컨트롤바 활성화
            }
        }
    }

    // Fragment 뷰가 소멸될 때 호출되는 함수
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
