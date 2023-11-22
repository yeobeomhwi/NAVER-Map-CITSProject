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
    //private val serviceKey = "Rp5a%2Bo7IpQ8GkuHupL0lqV6PSZmT3PaVE2Psd0tID1lSXjrroHKrnzpHnf%2B%2Fw4%2BLU9I8XubDag6kYCG6hu2rPA%3D%3D"
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

        val version = "*"

        CITSIdRepository.getCITSIdService(serviceKey, version,
            onSuccess = { citsIdResponse ->
                // 성공적으로 응답을 받았을 때 실행되는 부분
                val filteredItems = citsIdResponse?.body?.items?.filter { it.offerType == "SIG" }

                val filteredItemsCount = filteredItems?.size
                val linkIds = filteredItems?.map { it.linkId }?.joinToString(", ")
                Log.d("CITSResponse", "Success: filteredItemsCount = $filteredItemsCount")
                Log.d("CITSResponse", "Success: linkIds = $linkIds")

                linkIds?.split(", ")?.chunked(10)?.forEach { chunk ->
                    // Process each chunk of 10 linkIds
                    chunk.forEach { individualLinkId ->
                        // Convert the entire string to a numeric value
                        val numericLinkId = individualLinkId.filter { it.isDigit() }.toLongOrNull()

                        // Check if conversion was successful before using the value
                        if (numericLinkId != null) {
                            getCITSLocationService(numericLinkId.toString())
                            //Log.d("linkId_num", "individualLinkId: $numericLinkId")
                        } else {
                           // Log.e("linkId_num", "Conversion to numeric value failed: $individualLinkId")
                        }
                    }
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
                if(citsLocationResponse?.body != null){
                    val Itemss = citsLocationResponse?.body
                    Log.d("loc","fsdf= $Itemss")
                }

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
