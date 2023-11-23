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

<<<<<<< HEAD
class UlsanFragment : Fragment() {
    // ID_to_Firebase 인스턴스 생성
    private val idToFirebase = ID_to_Firebase()

    // Location_to_Firebase 인스턴스 생성
    private val locationToFirebase = Location_to_Firebase()

    // View Binding을 사용하기 위한 Binding 변수
=======
public class UlsanFragment : Fragment() {
    //private val serviceKey = "Rp5a%2Bo7IpQ8GkuHupL0lqV6PSZmT3PaVE2Psd0tID1lSXjrroHKrnzpHnf%2B%2Fw4%2BLU9I8XubDag6kYCG6hu2rPA%3D%3D"
    private val serviceKey = "TOlfl5zsDX0idc1uqdtoVkQkk7oSlUV+Mqks/OYbEuYjRtgy8j+4Vv4rrFOFQm9YHCIOlPr91KwSNqe0yJrSEg=="
    //private val serviceKey = "SSzwenl0pt9fP0H0Lzv+JTXtRoFXfzc9BHGAnOe9sRgnFUZ4wjmzRhrnOK1yEY0hNrThwhq3RpdIDzvH2h6Lgw=="

>>>>>>> 04966509537f212a7a5ad54da74320ac23724a7d
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

<<<<<<< HEAD
        // Location_to_Firebase fetchData 메서드 호출
        locationToFirebase.fetchData()

        return binding.root
    }

=======
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


>>>>>>> 04966509537f212a7a5ad54da74320ac23724a7d
    override fun onDestroyView() {
        super.onDestroyView()
        // View Binding 해제
        _binding = null
    }
}
