package com.example.cits_project

import android.os.Bundle
import android.view.Menu

import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.cits_project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // 뷰 바인딩을 위한 변수
    private lateinit var binding: ActivityMainBinding

    // 앱 바 구성을 위한 변수
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정
        setSupportActionBar(binding.appBarMain.toolbar)

        // 네비게이션 뷰와 네비게이션 컨트롤러 초기화
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // 앱 바 구성 초기화
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_finding_a_way, R.id.nav_slideshow
            ), drawerLayout
        )

        // 네비게이션 뷰와 앱 바 컨트롤러 설정
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 메뉴를 인플레이트하여 액션 바에 추가
        menuInflater.inflate(R.menu.main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        // 네비게이션 컨트롤러를 이용하여 뒤로 가기 동작 처리
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
