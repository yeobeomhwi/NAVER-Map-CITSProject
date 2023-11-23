package com.example.cits_project.ui.UlsanView

import android.util.Log
import com.example.cits_project.Ulsan.IdService.CITSIdRepository
import com.example.cits_project.databinding.FragmentUlsanBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ID_to_Firebase {
    // CITS API 서비스 키
    private val serviceKey = "TOlfl5zsDX0idc1uqdtoVkQkk7oSlUV+Mqks/OYbEuYjRtgy8j+4Vv4rrFOFQm9YHCIOlPr91KwSNqe0yJrSEg=="
    // Firebase Database 참조를 위한 변수
    private lateinit var database: DatabaseReference

    // Firebase에 저장할 데이터를 담는 데이터 클래스들
    data class FirebaseDataItem(
        val linkId: String? = null,
        val offerType: String? = null
    )

    data class FirebaseBody(
        var items: List<FirebaseDataItem> = emptyList(),
        val linkId: String? = null,
        val offerType: String? = null
    )

    data class FirebaseHeader(
        val resultCode: String? = null,
        val resultMsg: String? = null,
        var totalCnt: Int? = null,
        val signalTotalCnt: Int? = null
    )

    data class FirebaseBase(
        val body: FirebaseBody = FirebaseBody(),
        val header: FirebaseHeader = FirebaseHeader()
    )

    data class FirebaseData(
        val base: FirebaseBase = FirebaseBase()
    )

    // 데이터를 Firebase에서 가져와서 처리하는 메서드
    // 데이터를 Firebase에서 가져와서 처리하는 메서드
    fun fetchData() {
        // CITS API 호출을 위한 버전 정보
        val version = "*"

        // Firebase Database 참조 초기화
        database = Firebase.database.reference

        // Firebase에 저장할 데이터의 키
        val dataKey = "base-info"
        val dataRef = database.child("").child(dataKey)

        // CITS API 호출
        CITSIdRepository.getCITSIdService(serviceKey, version,
            onSuccess = { citsIdResponse ->
                // CITS API 응답을 Firebase 데이터 클래스로 변환
                val firebaseData = FirebaseData(
                    base = FirebaseBase(
                        header = FirebaseHeader(
                            resultCode = citsIdResponse?.header?.resultCode,
                            resultMsg = citsIdResponse?.header?.resultMsg,
                            totalCnt = citsIdResponse?.header?.totalCnt,
                            signalTotalCnt = citsIdResponse?.header?.signalTotalCnt
                        ),
                        body = FirebaseBody(
                            items = citsIdResponse?.body?.items
                                ?.filter { it.offerType == "SIG" } // "SIG"인 경우만 필터링
                                ?.map { item ->
                                    FirebaseDataItem(
                                        linkId = item.linkId,
                                        offerType = item.offerType
                                    )
                                } ?: emptyList(),
                            linkId = citsIdResponse?.body?.linkId,
                            offerType = citsIdResponse?.body?.offerType
                        )
                    )
                )

                // 키가 이미 존재하는지 확인
                dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // 기존 데이터가 존재하는 경우에도 새로운 데이터를 추가
                        if (snapshot.exists()) {
                            val existingData = snapshot.getValue(FirebaseData::class.java)

                            // 기존 데이터의 아이템 리스트
                            val existingItems = existingData?.base?.body?.items ?: emptyList()

                            // 새로운 아이템이 기존 아이템과 중복되지 않도록 필터링
                            val newItems = firebaseData.base.body.items?.filter { newItem ->
                                existingItems.none { it.linkId == newItem.linkId && it.offerType == newItem.offerType }
                            } ?: emptyList()

                            // 기존 아이템과 새로운 아이템을 합침
                            val updatedItems = existingItems.toMutableList().apply {
                                addAll(newItems)
                            }

                            // 새로운 데이터에 업데이트된 아이템 리스트 설정
                            firebaseData.base.body.items = updatedItems
                            // 새로운 데이터의 totalCnt를 실제 아이템의 개수로 설정
                            firebaseData.base.header.totalCnt = updatedItems.size

                            // 기존 데이터를 업데이트
                            snapshot.ref.setValue(firebaseData)
                            Log.d("ss", "Data updated for key: $dataKey")
                        } else {
                            // 기존 데이터가 없는 경우 새로운 데이터를 추가
                            snapshot.ref.setValue(firebaseData)
                            Log.d("ss", "Data added for key: $dataKey")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CITSResponse", "Database read error: $error")
                    }
                })


                Log.d("ss", "$citsIdResponse")
            }
        ) { error ->
            // 오류가 발생했을 때 실행되는 부분
            Log.e("CITSResponse", "Error: $error")
        }
    }

}
