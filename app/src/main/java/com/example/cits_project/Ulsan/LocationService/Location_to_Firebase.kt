package com.example.cits_project.Ulsan.LocationService

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import java.util.Objects

class Location_to_Firebase : CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val serviceKey = "TOlfl5zsDX0idc1uqdtoVkQkk7oSlUV+Mqks/OYbEuYjRtgy8j+4Vv4rrFOFQm9YHCIOlPr91KwSNqe0yJrSEg=="
    private lateinit var database: DatabaseReference
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    data class FirebaseDataItem(
        val ofer_Type: String? = null,
        val lttd: Double? = null,
        val lgtd: Double? = null,
        val link_id: String? = null
    )

    data class FirebaseBody(
        var items: List<FirebaseDataItem> = emptyList(),
    )

    data class FirebaseHeader(
        val resultCode: String? = null,
        var totalCnt: Int? = null,
        val requestUri: String? = null,
        val oferType: String? = null,
        val linkId: String? = null
    )

    data class FirebaseBase(
        val body: FirebaseBody = FirebaseBody()
    )


    fun fetchData() {
        // Firebase Database 참조 초기화
        database = Firebase.database.reference
        val dataKey = "base-info"
        val dataRef = database.child("")

        // totalCnt의 값을 가져오기 위한 참조
        val totalCntRef = dataRef.child("base-info").child("base").child("header").child("totalCnt")

        totalCntRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(totalCntSnapshot: DataSnapshot) {
                val totalCntValue = totalCntSnapshot.value
                var totalCnt = totalCntValue?.toString()?.toInt() ?: 0

                // 원하는 범위로 제한하세요. 예를 들어, 10으로 제한합니다.
                totalCnt = totalCnt.coerceIn(0, 1000)

                Log.d("linkId 개수", "$totalCnt")


                for (i in 0 until totalCnt) {
                    // 각 레벨에 대한 참조를 구성
                    val linkIdRef = dataRef.child("base-info").child("base").child("body").child("items").child(i.toString()).child("linkId")
                    Log.d("1","$linkIdRef")
                    linkIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // "linkID"로 수정
                            val linkId = snapshot.value?.toString()

                            if (!linkId.isNullOrBlank()) {
                                getCITSLocation(linkId)
                            } else {
                                Log.e("FetchData", "linkId가 null이거나 빈 문자열입니다.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FetchData", "Firebase 데이터베이스 오류: $error")
                        }
                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchData", "Firebase 데이터베이스 오류: $error")
            }
        })

    }

    private fun getCITSLocation(linkId: String) {
        Log.d("2", "Start of getCITSLocation for linkId: $linkId")
        database = Firebase.database.reference
        val dataKey = "Location-info"
        val dataRef = database.child(dataKey) // linkId를 사용하여 경로 생성

        val citsLocationRepository = CITSLocationRepository()

        citsLocationRepository.getCITSLocationService(
            serviceKey,
            linkId,
            { citsLocationResponse ->
                if (citsLocationResponse?.body != null) {
                    Log.d("getCITSLocation", "API 응답: $citsLocationResponse")

                    val firebaseData = FirebaseBase(
                        body = FirebaseBody(
                            items = citsLocationResponse.body?.items?.map { item ->
                                FirebaseDataItem(
                                    ofer_Type = item.ofer_Type,
                                    lttd = item.lttd,
                                    lgtd = item.lgtd,
                                    link_id = item.link_id
                                )
                            } ?: emptyList()
                        )
                    )
                    Log.d("33","$firebaseData")
                    // 키가 이미 존재하는지 확인
                    dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // 기존 데이터가 존재하는 경우에도 새로운 데이터를 추가
                            if (snapshot.exists()) {
                                val existingData = snapshot.getValue(Location_to_Firebase.FirebaseBase::class.java)

                                // 기존 데이터의 아이템 리스트
                                val existingItems = existingData?.body?.items ?: emptyList()

                                // 새로운 아이템이 기존 아이템과 중복되지 않도록 필터링
                                val newItems = firebaseData.body.items?.filter { newItem ->
                                    existingItems.none { it.link_id == newItem.link_id  }
                                } ?: emptyList()

                                // 기존 아이템과 새로운 아이템을 합침
                                val updatedItems = existingItems.toMutableList().apply {
                                    addAll(newItems)
                                }

                                // 새로운 데이터에 업데이트된 아이템 리스트 설정
                                firebaseData.body.items = updatedItems

                                // 기존 데이터를 업데이트
                                snapshot.ref.setValue(firebaseData)
                                Log.d("ss1", "Data updated for key: $dataKey")
                            } else {
                                // 기존 데이터가 없는 경우 새로운 데이터를 추가
                                snapshot.ref.setValue(firebaseData)
                                Log.d("ss2", "Data added for key: $dataKey")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("CITSResponse", "Database read error: $error")
                        }
                    })

                }
            },
            { error ->
                // 에러 처리
            }
        )
    }
}