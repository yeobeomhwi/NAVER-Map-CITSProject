package com.example.cits_project.Ulsan.IdService

import com.squareup.moshi.Json

data class CITSIdResponse(
    val header: CITSIdHeader,
    val body: CITSIdBody
)

data class CITSIdHeader(
    val resultCode: String,
    val resultMsg: String,
    val totalCnt: Int,
    val signalTotalCnt: Int?
)

data class CITSIdBody(
    val items: List<DataItem>,
    @Json(name = "link_id") val linkId: String?,
    @Json(name = "ofer_type") val offerType: String?
)

data class DataItem(
    @Json(name = "link_id") val linkId: String?,
    @Json(name = "ofer_type") val offerType: String?
)

