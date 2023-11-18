package com.example.cits_project.Search

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import android.text.Html

//검색 결과를 담는 데이터 클래스
@JsonClass(generateAdapter = true)
data class SearchResult(
    @field:Json(name="items") val items: List<SearchItem>
)

// 검색 결과의 각 항목을 담는 데이터 클래스
@JsonClass(generateAdapter = true)
data class SearchItem(
    @field:Json(name="title") val title: String,
    @field:Json(name="link") val link : String,
    @field:Json(name="category") val category : String,
    @field:Json(name="roadAddress") val roadAddress : String,
    @field:Json(name="mapx") val mapx : Int,
    @field:Json(name="mapy") val mapy : Int,
) {
    fun getCleanTitle(): String {
        // 타이틀에서 <b> 태그 제거한 정제된 문자열을 반환하는 메서드
        return title.replace("<b>", "").replace("</b>", "")
    }
}
