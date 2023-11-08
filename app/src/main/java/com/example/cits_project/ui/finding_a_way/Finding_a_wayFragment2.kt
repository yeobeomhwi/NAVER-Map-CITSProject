package com.example.cits_project.ui.finding_a_way


import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cits_project.NaverAPI
import com.example.cits_project.R
import com.example.cits_project.ResultPath
import com.example.cits_project.databinding.FragmentFindingAWay2Binding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Response


class Finding_a_wayFragment2 : Fragment(), OnMapReadyCallback {

    // 뷰 바인딩을 위한 변수
    private var _binding: FragmentFindingAWay2Binding? = null
    private val binding get() = _binding!!

    // NaverMap 객체, 마커, 정보 윈도우를 나타내는 변수들
    private lateinit var naverMap: NaverMap
    private lateinit var marker: Marker
    private lateinit var infoWindow: InfoWindow

    // 위치 정보 소스를 제공하는 변수
    private var locationSource: FusedLocationSource? = null

    // 위치 권한 요청 시 사용할 코드
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    val APIKEY_ID = "thps5vg7jo"
    val APIKEY = "h8DPvpdvUGhz3RkKwCTeo3aNmYOBC55Fw31sqNrT"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 초기화
        _binding = FragmentFindingAWay2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        // 위치 정보 소스 초기화 및 권한 요청 코드 설정
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // MapFragment를 찾아와서 비동기적으로 지도를 준비하도록 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mapFragment.getMapAsync(this)

        return root
    }


    override fun onMapReady(naverMap: NaverMap) {
        // onMapReady 콜백 함수에서 naverMap을 받아와서 멤버 변수로 설정합니다.
        this.naverMap = naverMap

        // 위치 정보 제공자를 설정합니다.
        naverMap.locationSource = locationSource

        // 주석 처리된 부분: 위치 추적 모드를 설정하는 코드입니다.
        // naverMap.locationTrackingMode = LocationTrackingMode.Face

        // 전달받은 출발지와 도착지 주소를 가져옵니다.
        val startLocation = arguments?.getString("start_location")
        val endLocation = arguments?.getString("end_location")

        // naverMap이 초기화된 후에 searchAddress를 호출하여 주소를 검색하고 표시합니다.
        searchAddress(startLocation, endLocation, binding.textView)

        // 아래부터는 naverMap에 대한 설정입니다.
        naverMap.apply {
            // 지도 유형을 네비게이션 모드로 설정합니다.
            mapType = NaverMap.MapType.Navi

            // 교통 정보, 건물 정보, 대중교통 정보 레이어를 활성화합니다.
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)

            // 건물의 높이를 설정합니다. (0.5배)
            buildingHeight = 0.5f

            // 심볼의 투시 비율을 설정합니다. (1배)
            symbolPerspectiveRatio = 1f

            // 현재 위치 좌표
            naverMap.addOnLocationChangeListener { location ->
                if (location != null) {
                    val currentLatLng = "현재위치 좌표: ${location.latitude}, ${location.longitude}"
                    println(currentLatLng)
                } else {
                    println("현재위치 좌표: Location unavailable")
                }
            }


            // 초기 카메라 위치를 설정합니다.
            val cameraPosition = CameraPosition(
                LatLng(0.0, 0.0), // 초기 위치는 (0,0) 위도와 경도입니다.
                18.0, // 줌 레벨은 18로 설정합니다.
                70.0, // 기울임 각도를 70도로 설정합니다.
                0.0 // 방향은 0도로 설정합니다.
            )

            // UI 설정을 적용합니다.
            uiSettings.apply {
                // 현재 위치 버튼을 활성화합니다.
                isLocationButtonEnabled = true

                // 기울임 제스처를 활성화합니다.
                isTiltGesturesEnabled = true

                // 회전 제스처를 활성화합니다.
                isRotateGesturesEnabled = true

                // 나침반을 비활성화합니다.
                isCompassEnabled = false

                // 줌 컨트롤을 활성화합니다.
                isZoomControlEnabled = true
            }
        }
    }


    // 주소에서 "대한민국" 부분을 제거하는 함수입니다.
    fun removeCountryFromAddress(address: String): String {
        // 주소를 공백을 기준으로 나눠서 각 부분을 리스트로 만듭니다.
        val parts = address.split(" ")

        // "대한민국"을 제외한 나머지 부분을 필터링합니다.
        val filteredParts = parts.filter { it != "대한민국" }

        // 필터링된 부분들을 다시 공백으로 연결하여 하나의 문자열로 만듭니다.
        return filteredParts.joinToString(" ")
    }

    // 수정된 searchAddress 함수
    fun searchAddress(startAddress: String?, endAddress: String?, textView: TextView?) {
        // Geocoder 객체를 초기화하여 주소와 좌표를 변환할 수 있도록 합니다.
        val geocoder = Geocoder(requireContext())

        // 시작 주소와 끝 주소에 대한 Address 객체 목록을 가져옵니다.
        val startList: List<Address>? = startAddress?.let { geocoder.getFromLocationName(it, 1) }
        val endList: List<Address>? = endAddress?.let { geocoder.getFromLocationName(it, 1) }

        // 시작 주소와 끝 주소가 모두 유효한 경우 실행합니다.
        if (startList != null && startList.isNotEmpty() && endList != null && endList.isNotEmpty()) {
            // Address 객체에서 위도와 경도를 추출합니다.
            val startLocation = LatLng(startList[0].latitude, startList[0].longitude)
            val endLocation = LatLng(endList[0].latitude, endList[0].longitude)

            // 주소에서 국가 이름을 제거합니다 (있는 경우).
            val startFullAddress = removeCountryFromAddress(startList[0].getAddressLine(0))
            val endFullAddress = removeCountryFromAddress(endList[0].getAddressLine(0))

            // 제공된 TextView에 시작 주소와 끝 주소를 표시합니다.
            textView?.let {
                val message = "출발지: $startFullAddress\n도착지: $endFullAddress"
                it.text = message
            }

            // 시작과 끝 위치의 중간 지점을 계산합니다.
            val middleLocation = LatLng(
                (startLocation.latitude + endLocation.latitude) / 2,
                (startLocation.longitude + endLocation.longitude) / 2
            )

            // 지도를 시작과 끝 위치를 포함하는 영역으로 줌 인 및 중앙 정렬합니다.
            val cameraUpdate =
                CameraUpdate.fitBounds(LatLngBounds.from(startLocation, endLocation), 100)
            naverMap.moveCamera(cameraUpdate)

            // 지도에 시작 위치와 끝 위치에 대한 마커를 추가합니다.
            val startMarker = Marker()
            startMarker.position = startLocation
            startMarker.map = naverMap

            val endMarker = Marker()
            endMarker.position = endLocation
            endMarker.map = naverMap

            // 시작 및 끝 위치의 위도와 경도를 출력합니다.
            val startLatLng = "출발지 좌표: ${startLocation.latitude}, ${startLocation.longitude}"
            val endLatLng = "도착지 좌표: ${endLocation.latitude}, ${endLocation.longitude}"
            println(startLatLng)
            println(endLatLng)

            // Naver Maps API로 HTTP 요청을 만들기 위해 Retrofit을 설정합니다.
            val retrofit = Retrofit.Builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com/map-direction/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(NaverAPI::class.java)

            // 시작 위치와 끝 위치 사이의 경로를 얻기 위한 요청을 만듭니다.
            val callgetPath = api.getPath(
                APIKEY_ID,
                APIKEY,
                "${startLocation.latitude}, ${startLocation.longitude}",
                "${endLocation.latitude}, ${endLocation.longitude}"
            )

            callgetPath.enqueue(object : Callback<ResultPath> {
                override fun onResponse(call: Call<ResultPath>, response: Response<ResultPath>) {
                    // API 응답을 처리합니다.

                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d("API_RESPONSE", responseBody.toString())
                    } else {
                        Log.d("API_RESPONSE", "Response body is null")
                    }
                    val path_cords_list = response.body()?.route?.traoptimal

                    if (path_cords_list != null) {
                        val path = PathOverlay()
                        val path_container: MutableList<LatLng>? = mutableListOf(LatLng(0.1, 0.1))
                        for (path_cords in path_cords_list) {
                            for (path_cords_xy in path_cords.path) {
                                path_container?.add(LatLng(path_cords_xy[1], path_cords_xy[0]))
                            }
                        }
                        path.coords = path_container?.drop(1)!!
                        path.color = Color.RED
                        path.map = naverMap

                        if (path.coords != null) {
                            val cameraUpdate = CameraUpdate.scrollTo(path.coords[0]!!)
                                .animate(CameraAnimation.Fly, 3000)
                            naverMap.moveCamera(cameraUpdate)

                            // 내비게이션이 시작됐음을 나타내는 토스트 메시지를 표시합니다.
                            Toast.makeText(
                                requireContext(),
                                "경로 안내가 시작됩니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // path_cords_list가 null일 때 처리하는 코드 추가
                    }
                }

                override fun onFailure(call: Call<ResultPath>, t: Throwable) {
                    // 요청이 실패한 경우 처리합니다.
                    Log.e("API_ERROR", "API 호출 실패: ${t.message}", t)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}