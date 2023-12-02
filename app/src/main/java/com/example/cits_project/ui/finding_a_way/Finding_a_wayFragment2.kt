package com.example.cits_project.ui.finding_a_way

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Color.argb
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentFindingAWay2Binding
import com.example.cits_project.ui.finding_a_way.api.NaverAPI
import com.example.cits_project.ui.finding_a_way.api.ResultPath
import com.example.cits_project.ui.finding_a_way.api.Result_path
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.location.Location
import android.util.Log
import android.widget.Toast
import retrofit2.http.POST
import retrofit2.http.Query
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


data class LatLngResponse(
    val x:String? = null,
    val y:String? = null,
    val traffic:String? = null
    // 기타 필요한 필드
)

interface ApiService {
    @POST("/get_lat_lng")
    fun getLatLng(
        @Query("x") x: Double,
        @Query("y") y: Double
    ): Call<LatLngResponse> // 네트워크 요청의 반환 타입을 명시해줍니다.
}


class Finding_a_wayFragment2 : Fragment(), OnMapReadyCallback {

    // View binding을 위한 변수
    private var _binding: FragmentFindingAWay2Binding? = null
    private val binding get() = _binding!!

    // 경로 좌표를 담을 리스트
    private var path_cords_list: List<Result_path> = emptyList()

    // NaverMap 객체
    private lateinit var naverMap: NaverMap

    // 위치 정보를 가져오기 위한 FusedLocationSource
    private var locationSource: FusedLocationSource? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    // 속력을 표시할 TextView
    private lateinit var speedTextView: TextView

    // Naver Map 서비스를 위한 API 키
    val APIKEY_ID = "thps5vg7jo"
    val APIKEY = "h8DPvpdvUGhz3RkKwCTeo3aNmYOBC55Fw31sqNrT"

    // Retrofit을 초기화하고 API 인터페이스를 생성
    val retrofit = Retrofit.Builder().
    baseUrl("https://naveropenapi.apigw.ntruss.com/map-direction/").
    addConverterFactory(GsonConverterFactory.create()).
    build()
    val api = retrofit.create(NaverAPI::class.java)

    // 람다 외부에서 좌표 변수 선언
    lateinit var startLocation: LatLng
    lateinit var endLocation: LatLng

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // View binding 초기화
        _binding = FragmentFindingAWay2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        // 위치 권한 요청을 위한 FusedLocationSource 초기화
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // MapFragment를 초기화하고 지도를 로드하는 비동기 작업을 시작
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mapFragment.getMapAsync(this)

        // speedTextView를 초기화합니다.
        speedTextView = binding.speedTextView

        val findingAWayButton = root.findViewById<Button>(R.id.Finding_a_way_button)
        findingAWayButton.setOnClickListener { searchAddress ->
            // 받아온 argument에서 데이터 추출
            val sname = arguments?.getString("start_location")
            val dname = arguments?.getString("end_location")

            val appname = "com.example.cits_project"

            // 네이버 지도 호출을 위한 URL 생성
            val url = "nmap://navigation?&slat=${startLocation.latitude}&slng=${startLocation.longitude}&sname=$sname&dlat=${endLocation.latitude}&dlng=${endLocation.longitude}&dname=${Uri.encode(dname)}&appname=$appname"

            // 로그로 URL 확인
            Log.d("NavigationDebug", "startLocation: $startLocation")
            Log.d("NavigationDebug", "endLocation: $endLocation")
            Log.d("NavigationDebug", "sname: $sname")
            Log.d("NavigationDebug", "dname: $dname")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)

            val list: List<ResolveInfo> =
                requireActivity().packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            if (list == null || list.isEmpty()) {
                requireContext().startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.nhn.android.nmap")
                    )
                )
            } else {
                requireContext().startActivity(intent)
            }
        }

        return root
    }

    // 속력 정보를 업데이트하는 함수
    private fun updateSpeed(speed: Float) {
        val speedInt = speed.toInt() // 속력을 정수로 변환
        speedTextView.text = "${speedInt}"
    }

    override fun onMapReady(naverMap: NaverMap) {
        // NaverMap 객체를 초기화하고 현재 위치를 가져올 수 있도록 설정
        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        // 시작 지점과 도착 지점을 가져오고 주소를 검색
        val startLocationStr = arguments?.getString("start_location")
        val endLocationStr = arguments?.getString("end_location")

        if (startLocationStr != null && endLocationStr != null) {
            // 좌표 초기화
            startLocation = getLatLngFromAddress(startLocationStr)
            endLocation = getLatLngFromAddress(endLocationStr)

            searchAddress(startLocationStr, endLocationStr, binding.textView)
        }





        // NaverMap 설정
        naverMap.apply {
            mapType = NaverMap.MapType.Navi // 지도 유형 설정 (Navi: 네비게이션 모드)
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true) // 교통 정보 레이어
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true) // 건물 정보 레이어
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true) // 대중교통 정보 레이어
            buildingHeight = 0.5f // 건물 높이 설정
            symbolPerspectiveRatio = 1f // 심볼 원근 비율 설정

            // 현재 위치가 변경될 때 실행되는 리스너 설정
            addOnLocationChangeListener { location ->
                if (location != null) {
                    val currentLatLng =
                        "현재위치 좌표: ${location.latitude}, ${location.longitude}"
                    println(currentLatLng)
                } else {
                    println("현재위치 좌표: Location unavailable")
                }
            }

            // 초기 카메라 위치 설정
            val cameraPosition = CameraPosition(
                LatLng(0.0, 0.0),
                18.0,
                70.0,
                0.0
            )

            // UI 설정
            uiSettings.apply {
                isLocationButtonEnabled = true // 현재 위치 버튼
                isTiltGesturesEnabled = true // 지도를 기울이는 제스처
                isRotateGesturesEnabled = true // 지도를 회전하는 제스처
                isCompassEnabled = false // 나침반
                isZoomControlEnabled = true // 줌 컨트롤
            }
        }

        // 현재 위치가 변경될 때 실행되는 리스너 설정
        naverMap.addOnLocationChangeListener { location ->
            if (location != null) {
                val currentLatLng =
                    "현재위치 좌표: ${location.latitude}, ${location.longitude}"
                println(currentLatLng)

                // 현재 속력을 업데이트합니다.
                updateSpeed(location.speed)
            } else {
                println("현재위치 좌표: Location unavailable")
            }
        }
    }

    private fun getLatLngFromAddress(address: String): LatLng {
        val geocoder = Geocoder(requireContext())
        val addressList: List<Address>?

        try {
            // 예외가 발생할 수 있는 부분을 try-catch 블록으로 감싸줍니다.
            addressList = geocoder.getFromLocationName(address, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val latitude = addressList[0].latitude
                val longitude = addressList[0].longitude
                return LatLng(latitude, longitude)
            } else {
                // 주소 변환 실패 시 처리
                throw IllegalArgumentException("주소를 LatLng으로 변환할 수 없습니다.")
            }
        } catch (e: Exception) {
            // 예외가 발생하면 콘솔에 로그 출력 및 Toast 메시지 표시
            Log.e("AddressConversion", "Error converting address to LatLng: ${e.message}")
            Toast.makeText(requireContext(), "주소를 LatLng으로 변환할 수 없습니다.", Toast.LENGTH_SHORT).show()

            // 기본값으로 0, 0 좌표를 반환하거나 원하는 처리를 추가할 수 있습니다.
            return LatLng(0.0, 0.0)
        }
    }

    fun removeCountryFromAddress(address: String): String {
        // 주소를 공백을 기준으로 나눕니다.
        val parts = address.split(" ")

        // "대한민국"을 필터링하여 리스트에서 제거합니다.
        val filteredParts = parts.filter { it != "대한민국" }

        // 필터링된 부분들을 다시 공백을 이용하여 문자열로 합칩니다.
        return filteredParts.joinToString(" ")
    }

    fun searchAddress(startAddress: String?, endAddress: String?, textView: TextView?) {
        // Geocoder 객체를 생성합니다.
        val geocoder = Geocoder(requireContext())

        // 시작 주소와 끝 주소를 기반으로 위치 정보를 가져옵니다.
        val startList: List<Address>? = startAddress?.let { geocoder.getFromLocationName(it, 1) }
        val endList: List<Address>? = endAddress?.let { geocoder.getFromLocationName(it, 1) }

        // 만약 시작 주소와 끝 주소 모두 유효한 경우
        if (startList != null && startList.isNotEmpty() && endList != null && endList.isNotEmpty()) {
            // 시작 위치와 끝 위치의 좌표를 LatLng 객체로 생성합니다.
            val startLocation = LatLng(startList[0].latitude, startList[0].longitude)
            val endLocation = LatLng(endList[0].latitude, endList[0].longitude)

            // 주소에서 "대한민국"을 제거한 전체 주소를 가져옵니다.
            val startFullAddress = removeCountryFromAddress(startList[0].getAddressLine(0))
            val endFullAddress = removeCountryFromAddress(endList[0].getAddressLine(0))

            // TextView에 출발지와 도착지 정보를 설정합니다.
            textView?.let {
                val message = "출발지: $startFullAddress\n도착지: $endFullAddress"
                it.text = message
            }

            // 출발지와 도착지를 포함하는 경계를 계산하여 지도를 이동합니다.
            val cameraUpdate = CameraUpdate.fitBounds(LatLngBounds.from(startLocation, endLocation), 100)
            naverMap.moveCamera(cameraUpdate)

            // 공통 마커 생성 함수
           fun createMarker(position: LatLng, caption: String, icon: OverlayImage, iconTintColor: Int): Marker {
                val marker = Marker()
                marker.position = position
                marker.captionText = caption //글씨
                marker.setCaptionAligns(Align.Top) // 텍스트 위치를 가운데 위로
                marker.captionTextSize = 16f // 글씨 크기
                marker.icon = icon //바탕색
                marker.iconTintColor = iconTintColor //보이는색
                marker.alpha = 0.5f
                return marker
            }

            // 출발지와 도착지에 마커를 추가합니다.
            val startMarker = createMarker(startLocation, "출발지", MarkerIcons.BLACK, Color.RED)
            startMarker.map = naverMap

            val endMarker = createMarker(endLocation, "도착지", MarkerIcons.BLACK, Color.BLUE)
            endMarker.map = naverMap


            // 출발지와 도착지의 좌표를 출력합니다.
            val startLatLng = "출발지 좌표: ${startLocation.latitude}, ${startLocation.longitude}"
            val endLatLng = "도착지 좌표: ${endLocation.latitude}, ${endLocation.longitude}"
            println(startLatLng)
            println(endLatLng)

            // 출발지 신호등 확인 API
            val retrofit = Retrofit.Builder()
                .baseUrl("http://172.30.1.12:6000/") // 기본 URL 설정
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS) // 연결 대기 시간 조정
                        .readTimeout(30, TimeUnit.SECONDS) // 읽기 대기 시간 조정
                        .writeTimeout(30, TimeUnit.SECONDS) // 쓰기 대기 시간 조정
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create()) // Gson 컨버터 팩토리 추가
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val call = apiService.getLatLng(startLocation.latitude, startLocation.longitude)
            call.enqueue(object : retrofit2.Callback<LatLngResponse> {
                override fun onResponse(
                    call: Call<LatLngResponse>,
                    response: retrofit2.Response<LatLngResponse>
                ) {
                    if(response.isSuccessful){
                        val data = response.body()
                        data?.let {
                            val xValue = it.x ?: "N/A"
                            val yValue = it.y ?: "N/A"
                            val trafficValue = it.traffic ?: "N/A"

                            val logMessage = "x=$xValue, y=$yValue, traffic=$trafficValue"
                            Log.d("DATA", logMessage)
                            val traffic_Location = LatLng(xValue.toDouble(), yValue.toDouble())
                            val traffic_Marker = createMarker(traffic_Location, trafficValue.toString(), MarkerIcons.BLACK, Color.GREEN)
                            traffic_Marker.map = naverMap
                        }
                    }
                    Log.d("log", response.toString())
                    Log.d("log", response.body().toString())


                }

                override fun onFailure(call: Call<LatLngResponse>, t: Throwable) {
                    Log.e("Error", "Error : ${t.message}")
                }
            })


            // 데이터를 다른 프래그먼트로 전달하기 위한 Bundle을 생성합니다.
            val bundle = Bundle()
            bundle.putString("start_LatLng", startLatLng)
            bundle.putString("end_LatLng", endLatLng)

            // 경로 정보를 요청합니다.
            val callgetPath = api.getPath(
                APIKEY_ID,
                APIKEY,
                "${startLocation.longitude}, ${startLocation.latitude}",
                "${endLocation.longitude}, ${endLocation.latitude}",
                "traoptimal" //실시간 최적은 되는데 실시간 빠른길이 안댐
            )

            callgetPath.enqueue(object : Callback<ResultPath> {
                override fun onResponse(
                    call: Call<ResultPath>,
                    response: Response<ResultPath>
                ) {
                    if (response.isSuccessful) {
                        val resultPath = response.body()
                        // 로그로 응답 내용을 출력합니다.
                        Log.d("ServerResponse1", "Response: ${resultPath?.toString()}")
                        Log.d("ServerResponse1", "Response: ${response?.isSuccessful()}")

                        // 1. 응답에서 경로 정보를 추출합니다.
                        val traoptimal = resultPath?.route?.traoptimal

                        if (traoptimal != null) {
                            path_cords_list = traoptimal

                            // 2. PathOverlay를 초기화합니다.
                            val path = PathOverlay()

                            // 3. 경로를 표현할 좌표들을 담을 리스트를 생성합니다.
                            val path_container: MutableList<LatLng>? = mutableListOf()

                            // 4. 경로 정보를 순회합니다.
                            for (path_cords in path_cords_list!!) {
                                // 각 경로의 좌표 정보를 순회합니다.
                                for (path_cords_xy in path_cords?.path!!) {
                                    // 경로 좌표를 LatLng 객체로 변환하고 리스트에 추가합니다.
                                    path_container?.add(
                                        LatLng(
                                            path_cords_xy[1],
                                            path_cords_xy[0]
                                        )
                                    )
                                }
                            }

                            // 5. PathOverlay에 좌표 정보를 설정합니다.
                            path.coords = path_container ?: emptyList()

                                // 6. 경로의 색상을 검은색 테두리를 노란색으로 지정합니다.
                                path.color = Color.GRAY
                                path.width = 15
                                path.passedColor = Color.argb(0,0,0,0)

                                // 7. 지도에 경로를 추가하여 표시합니다.
                                path.map = naverMap

                                // 8. 경로 정보가 있을 경우, 시작 위치로 지도를 이동시킵니다.
                                if (path.coords.isNotEmpty()) {
                                    // 경로의 첫 번째 좌표로 카메라 이동을 설정합니다.
                                    val cameraUpdate = CameraUpdate.scrollAndZoomTo(path.coords[0]!!,17.0)
                                        .animate(CameraAnimation.Fly, 5000)

                                    // 카메라 이동을 적용합니다.
                                    naverMap.moveCamera(cameraUpdate)

                                    // 안내 메시지를 표시합니다.
                                    Toast.makeText(requireContext(), "경로 안내가 시작됩니다.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // traoptimal이 null일 경우 처리
                                Log.d("ServerResponse3", "traoptimal is null")
                                Log.d("ServerResponse3","Response: ${response?.body()}")
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResultPath>, t: Throwable) {
                        // 에러 처리
                        Log.e("ServerResponse4", "Error: ${t.message}")
                    }
                })
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
