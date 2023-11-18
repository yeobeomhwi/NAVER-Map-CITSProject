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

        fun bind(item: SearchItem) {
            val cleanTitle = item.getCleanTitle()
            binding.titleTextView.text = cleanTitle
            binding.categoryTextView.text = item.category
            binding.locationTextView.text = item.roadAddress

            binding.root.setOnClickListener {
                onClick(LatLng(
                    item.mapy.toDouble() / 10000000.0,  // 위도
                    item.mapx.toDouble() / 10000000.0   // 경도
                ))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataSet: List<SearchItem>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}

// 타이틀에서 HTML 태그를 제거하여 정리하는 확장 함수
fun SearchItem.getCleanTitle(): String {
    return Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString()
}
