package com.example.cits_project.ui.finding_a_way.api

import SearchHistoryItem
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cits_project.databinding.SearchHistoryItemBinding

class SearchHistoryAdapter(
    private var searchHistory: List<SearchHistoryItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    // 뷰홀더를 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // LayoutInflater를 사용하여 XML 레이아웃 파일을 객체로 변환
        val inflater = LayoutInflater.from(parent.context)
        val binding = SearchHistoryItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    // 뷰홀더에 데이터를 바인딩하는 함수
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 뷰홀더에 현재 포지션의 데이터를 바인딩
        holder.bind(searchHistory[position])
    }

    // 데이터 목록의 크기를 반환하는 함수
    override fun getItemCount(): Int {
        return searchHistory.size
    }

    // 외부에서 데이터 목록을 받아와 어댑터에 설정하는 함수
    fun submitList(newSearchHistory: List<SearchHistoryItem>?) {
        // 새로운 데이터로 목록을 갱신하고 어댑터에 알림
        searchHistory = newSearchHistory ?: emptyList()
        notifyDataSetChanged()
    }

    // 뷰홀더 클래스 정의
    inner class ViewHolder(private val binding: SearchHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // 아이템 뷰가 클릭되었을 때의 동작 정의
            binding.root.setOnClickListener {
                // 클릭된 아이템의 데이터를 리스너를 통해 외부로 전달
                listener.onItemClick(searchHistory[adapterPosition])
            }
        }

        // 뷰홀더에 데이터를 바인딩하는 함수
        fun bind(item: SearchHistoryItem) {
            // 아이템의 데이터를 뷰에 설정
            binding.startTextView.text = item.startLocation
            binding.endTextView.text = item.endLocation
        }
    }

    // 아이템 클릭 리스너 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClick(item: SearchHistoryItem)
    }
}
