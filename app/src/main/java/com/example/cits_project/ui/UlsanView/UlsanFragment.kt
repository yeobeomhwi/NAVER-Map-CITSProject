package com.example.cits_project.ui.UlsanView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cits_project.Ulsan.IdService.CITSIdRepository
import com.example.cits_project.Ulsan.LocationService.CITSLocationRepository
import com.example.cits_project.Ulsan.LocationService.CITSLocationResponse
import com.example.cits_project.databinding.FragmentSlideshowBinding

public class UlsanFragment : Fragment() {
    private val serviceKey = "TOlfl5zsDX0idc1uqdtoVkQkk7oSlUV+Mqks/OYbEuYjRtgy8j+4Vv4rrFOFQm9YHCIOlPr91KwSNqe0yJrSEg=="
    //private val serviceKey = "SSzwenl0pt9fP0H0Lzv+JTXtRoFXfzc9BHGAnOe9sRgnFUZ4wjmzRhrnOK1yEY0hNrThwhq3RpdIDzvH2h6Lgw=="
    private var _binding: FragmentSlideshowBinding? = null
    private val locationRepository = CITSLocationRepository()
    private val binding get() = _binding!!

    private val slideshowViewModel by lazy {
        ViewModelProvider(this).get(UlsanViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow

        // 호출할 API에 필요한 매개변수 설정 (예: serviceKey, version)

        val version = "20221026164946"

        // CITS API 호출
        CITSIdRepository.getCITSIdService(serviceKey, version,
            onSuccess = { citsIdResponse ->
                // 성공적으로 응답을 받았을 때 실행되는 부분
                val filteredItems = citsIdResponse?.body?.items?.filter { it.offerType == "SIG" }

                val filteredItemsCount = filteredItems?.size
                val linkId = filteredItems?.map { it.linkId }?.joinToString(", ")
                Log.d("CITSResponse", "Success: link_ids = $linkId")
                Log.d("filteredItemsCount", "Success: filteredItemsCount = $filteredItemsCount")


                // linkId가 null이 아닌 경우에만 getCITSLocationService 호출
                var maxIndex = -1 // 최대값을 추적할 변수 초기화

                if (linkId != null) {
                    for (i in 0 until (filteredItemsCount ?: 0)) {
                        val currentLinkId = filteredItems[i].linkId
                        if (currentLinkId != null) {
                            getCITSLocationService(currentLinkId)
                        }
                        // if (i > maxIndex) {
                        //     maxIndex = i
                        // }
                    }
                    // Log.d("max", "max = $maxIndex")
                }
            }
        ) { error ->
            // 오류가 발생했을 때 실행되는 부분
            Log.e("CITSResponse", "Error: $error")
        }
        return root
    }

    private fun getCITSLocationService(linkId: String) {
        // linkId와 type을 활용하여 getCITSLocationService 호출
        locationRepository.getCITSLocationService(serviceKey, linkId,
            onSuccess = { citsLocationResponse ->

                Log.d("loc","fsdf= $citsLocationResponse")
                // 성공적으로 응답을 받았을 때 실행되는 부분
                // citsLocationResponse에 포함된 데이터 활용
                // 예시: val lttd = citsLocationResponse?.body?.lttd
                // 예시: val lgtd = citsLocationResponse?.body?.lgtd

                // 받은 데이터를 원하는 방식으로 처리
                // 예시: textView.text = "Latitude: $lttd, Longitude: $lgtd"
            }
        ) { error ->
            // 오류가 발생했을 때 실행되는 부분
            Log.e("CITSLocationResponse", "Error: $error")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
