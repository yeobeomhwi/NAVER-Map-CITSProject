package com.example.cits_project.ui.finding_a_way

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.cits_project.R
import com.example.cits_project.databinding.FragmentFindingAWayBinding

class Finding_a_wayFragment : Fragment() {

    private var _binding: FragmentFindingAWayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val finding_a_wayViewModel =
            ViewModelProvider(this).get(Finding_a_wayViewModel::class.java)

        _binding = FragmentFindingAWayBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val btnFindRoute = root.findViewById<Button>(R.id.btn_find_route)
        val etStartLocation = root.findViewById<EditText>(R.id.et_start_location)
        val etEndLocation = root.findViewById<EditText>(R.id.et_end_location)

        btnFindRoute.setOnClickListener {
            val startLocation = etStartLocation.text.toString()
            val endLocation = etEndLocation.text.toString()

            val bundle = Bundle()
            bundle.putString("start_location", startLocation)
            bundle.putString("end_location", endLocation)

            val navController = findNavController()
            navController.navigate(R.id.action_nav_finding_a_way_to_nav_finding_a_way2, bundle)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
