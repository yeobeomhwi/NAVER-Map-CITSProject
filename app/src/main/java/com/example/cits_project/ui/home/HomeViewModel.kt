package com.example.cits_project.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "초기 화면입니다. 현재위치 찾기"
    }
    val text: LiveData<String> = _text
}