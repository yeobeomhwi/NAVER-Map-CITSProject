package com.example.cits_project.ui.notice
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cits_project.R

class NoticeFragment : Fragment() {

    private lateinit var alertSwitch: Switch
    private lateinit var soundSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notice, container, false)
        // UI 요소 초기화
        alertSwitch = view.findViewById(R.id.alertSwitch)
        soundSwitch = view.findViewById(R.id.soundSwitch)

        // SharedPreferences 초기화
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // 저장된 설정 불러오기
        loadUserSettings()

        // 스위치 상태 변경 리스너 등록
        alertSwitch.setOnCheckedChangeListener { _, isChecked ->
            // 사용자 설정 저장
            saveUserSetting(KEY_ALERT, isChecked)
            // 토스트 알림 표시
            showToast("Alerts are ${if (isChecked) "enabled" else "disabled"}")
        }

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            // 사용자 설정 저장
            saveUserSetting(KEY_SOUND, isChecked)
            // 토스트 알림 표시
            showToast("Sound is ${if (isChecked) "enabled" else "disabled"}")
        }

        return view
    }

    private fun loadUserSettings() {
        // 저장된 설정 불러오기
        val alertEnabled = readUserSetting(KEY_ALERT, true)
        val soundEnabled = readUserSetting(KEY_SOUND, true)

        // 불러온 설정을 UI에 반영
        alertSwitch.isChecked = alertEnabled
        soundSwitch.isChecked = soundEnabled
    }

    private fun saveUserSetting(key: String, value: Boolean) {
        // 사용자 설정 저장
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private fun readUserSetting(key: String, defaultValue: Boolean): Boolean {
        // 사용자 설정 불러오기
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val KEY_ALERT = "alert_enabled"
        const val KEY_SOUND = "sound_enabled"
        const val PREF_NAME = "user_settings"
    }
}