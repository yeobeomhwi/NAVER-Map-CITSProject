package com.example.cits_project.ui.finding_a_way

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentFindingAWayBinding

class Finding_a_wayFragment : Fragment() {

    // Fragment의 바인딩을 저장할 변수를 선언합니다.
    private var _binding: FragmentFindingAWayBinding? = null

    // 바인딩에 접근하기 위한 프로퍼티를 생성합니다. null 안전성을 활용합니다.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewModelProvider를 사용하여 ViewModel의 인스턴스를 생성합니다.
        val finding_a_wayViewModel = ViewModelProvider(this).get(Finding_a_wayViewModel::class.java)

        // 바인딩 객체를 사용하여 레이아웃을 inflate합니다.
        _binding = FragmentFindingAWayBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // UI 요소들(Button과 EditText)에 대한 참조를 가져옵니다.
        val btnFindRoute = root.findViewById<Button>(R.id.btn_find_route)
        val etStartLocation = root.findViewById<EditText>(R.id.et_start_location)
        val etEndLocation = root.findViewById<EditText>(R.id.et_end_location)
        val btnStartClear = root.findViewById<ImageButton>(R.id.start_clear_button)
        val btnEndClear = root.findViewById<ImageButton>(R.id.end_clear_button)
        val btnChange = root.findViewById<ImageButton>(R.id.Change_button)

        btnFindRoute.setOnClickListener {
            // EditText에서 텍스트를 가져옵니다.
            val startLocation = etStartLocation.text.toString()
            val endLocation = etEndLocation.text.toString()

            // 데이터를 다른 프래그먼트로 전달하기 위한 Bundle을 생성합니다.
            val bundle = Bundle()
            bundle.putString("start_location", startLocation)
            bundle.putString("end_location", endLocation)

            // 지정된 액션을 사용하여 다른 목적지로 이동하고 Bundle을 전달합니다.
            val navController = findNavController()
            navController.navigate(R.id.action_nav_finding_a_way_to_nav_finding_a_way2, bundle)
        }

        btnStartClear.setOnClickListener{
            //출발지 내용 삭제
            etStartLocation.text.clear()
        }

        btnEndClear.setOnClickListener {
            //도착지 내용삭제
            etEndLocation.text.clear()
        }

        btnChange.setOnClickListener {
            //출발지 도착지 내용 교환
            // 현재의 출발지와 도착지를 가져옵니다.
            val currentStartLocation = etStartLocation.text.toString()
            val currentEndLocation = etEndLocation.text.toString()

            // 출발지와 도착지를 교환하여 EditText에 설정합니다.
            etStartLocation.setText(currentEndLocation)
            etEndLocation.setText(currentStartLocation)
        }

        return root
    }

    // onDestroyView를 오버라이드하여 바인딩을 정리합니다.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
