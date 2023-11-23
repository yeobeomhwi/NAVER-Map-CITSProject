package com.example.cits_project.ui.UlsanView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cits_project.Ulsan.LocationService.Location_to_Firebase
import com.example.cits_project.databinding.FragmentSlideshowBinding
import com.google.firebase.database.DatabaseReference

class UlsanFragment : Fragment() {
    // ID_to_Firebase 인스턴스 생성
    private val idToFirebase = ID_to_Firebase()

    // Location_to_Firebase 인스턴스 생성
    private val locationToFirebase = Location_to_Firebase()

    // View Binding을 사용하기 위한 Binding 변수
    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    // Firebase Database 참조를 위한 변수
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // UlsanViewModel 인스턴스 생성
        val UlsanViewModel =
            ViewModelProvider(this).get(UlsanViewModel::class.java)

        // View Binding 초기화
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)

        // ID_to_Firebase의 fetchData 메서드 호출
        //idToFirebase.fetchData()

        // Location_to_Firebase fetchData 메서드 호출
        locationToFirebase.fetchData()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // View Binding 해제
        _binding = null
    }
}