package com.example.cits_project.ui.finding_a_way

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val distance: Int,
    val duration: Int,
    // 더 많은 필드들을 필요에 따라 추가할 수 있습니다.
)
