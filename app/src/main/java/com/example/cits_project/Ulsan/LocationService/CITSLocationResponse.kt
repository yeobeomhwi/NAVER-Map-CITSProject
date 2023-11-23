package com.example.cits_project.Ulsan.LocationService

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CITSLocationResponse(
    @Json(name = "header")
    val header: Header?,

    @Json(name = "body")
    val body: Body?
)

@JsonClass(generateAdapter = true)
data class Header(
    @Json(name = "resultCode")
    val resultCode: String?,

    @Json(name = "totalCnt")
    val totalCnt: Int?,

    @Json(name = "requestUri")
    val requestUri: String?,

    @Json(name = "oferType")
    val oferType: String?,

    @Json(name = "linkId")
    val linkId: String?
)

@JsonClass(generateAdapter = true)
data class Body(
    @Json(name = "items")
    val items: List<Item>?
)

@JsonClass(generateAdapter = true)
data class Item(
    @Json(name = "ofer_type")
    val ofer_Type: String?,

    @Json(name = "lttd")
    val lttd: Double?,

    @Json(name = "lgtd")
    val lgtd: Double?,

    @Json(name = "link_id")
    val link_id: String?

)
