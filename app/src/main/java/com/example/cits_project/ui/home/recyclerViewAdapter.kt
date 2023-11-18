package com.example.cits_project.ui.home

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cits_project.Search.SearchItem
import com.example.cits_project.databinding.RecyclerviewItemBinding
import com.naver.maps.geometry.LatLng

class recyclerViewAdapter(private val onClick: (LatLng) -> Unit) : RecyclerView.Adapter<recyclerViewAdapter.ViewHolder>() {

    private var dataSet = emptyList<SearchItem>()

    inner class ViewHolder(private val binding: RecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // 뷰홀더에 데이터를 바인딩하는 함수
        fun bind(item: SearchItem) {
            // 태그가 제거된 정제된 타이틀 설정
            val cleanTitle = item.getCleanTitle()
            binding.titleTextView.text = cleanTitle
            // 카테고리와 위치 정보 설정
            binding.categoryTextView.text = item.category
            binding.locationTextView.text = item.roadAddress

            // 아이템 뷰가 클릭되었을 때의 동작 정의
            binding.root.setOnClickListener {
                // 클릭된 아이템의 위도와 경도를 포함하여 콜백 호출
                onClick(LatLng(
                    item.mapy.toDouble() / 10000000.0,  // 위도
                    item.mapx.toDouble() / 10000000.0   // 경도
                ))
            }
        }
    }

    // 뷰홀더를 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 뷰홀더 객체를 생성하여 반환
        return ViewHolder(
            RecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    // 뷰홀더에 데이터를 바인딩하는 함수
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 뷰홀더에 현재 포지션의 데이터를 바인딩
        holder.bind(dataSet[position])
    }

    // 데이터 목록의 크기를 반환하는 함수
    override fun getItemCount(): Int = dataSet.size

    // 데이터 목록을 갱신하는 함수
    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataSet: List<SearchItem>) {
        // 외부에서 받아온 새로운 데이터로 목록을 갱신하고 어댑터에 알림
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}

// 타이틀에서 HTML 태그를 제거하여 정리하는 확장 함수
fun SearchItem.getCleanTitle(): String {
    return Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString()
}
