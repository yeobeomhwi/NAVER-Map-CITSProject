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
import com.example.cits_project.Ulsan.LocationService.Location_to_Firebase
import com.example.cits_project.databinding.FragmentUlsanBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.ZoomControlView


class UlsanFragment : Fragment() {

    private var _binding: FragmentUlsanBinding? = null
    private val locationToFirebase = Location_to_Firebase()

    private val binding get() = _binding!!

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
        _binding = FragmentUlsanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 뷰 반환
        return root
    }


    // Fragment 뷰가 소멸될 때 호출되는 함수
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
