package com.example.cits_project.Search

import android.app.Application
import android.content.Context

// 애플리케이션 클래스를 상속받은 사용자 정의 애플리케이션 클래스
class MyApplication : Application() {

    // 애플리케이션 생성 시 호출되는 메서드
    override fun onCreate() {
        super.onCreate()
        // 애플리케이션 컨텍스트를 정적 변수에 할당
        Companion.applicationContext = applicationContext

    }

    // 정적 변수와 메서드를 가지는 동반 객체 정의
    companion object {
        // 애플리케이션 컨텍스트를 저장하는 변수
        lateinit var applicationContext: Context
    }
}
