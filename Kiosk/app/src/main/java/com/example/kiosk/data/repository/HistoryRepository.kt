package com.example.kiosk.data.repository

import android.content.Context
import com.example.kiosk.data.model.HistoryRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// 역할: 학습 기록 데이터를 기기 내부 저장소(SharedPreferences)에 저장하고 불러오는 관리자
class HistoryRepository(context: Context) {
    // 'kiosk_prefs'라는 이름의 저장소 공간을 사용
    private val prefs = context.getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)
    private val gson = Gson() // 객체를 JSON 문자열로 변환하기 위한 도구

    // 저장된 모든 학습 기록 불러오기
    fun getAllHistory(): List<HistoryRecord> {
        // 저장된 JSON 문자열 가져오기
        val json = prefs.getString("kioskLearningHistory", null)

        // 저장된 게 없으면 빈 리스트 반환
        if (json == null) {
            return emptyList()
        }

        // JSON 문자열을 HistoryRecord 리스트 객체로 변환
        val type = object : TypeToken<List<HistoryRecord>>() {}.type
        return gson.fromJson(json, type)
    }

    // 새로운 학습 기록 저장하기
    fun saveHistory(record: HistoryRecord) {
        // 1. 기존 기록 불러오기
        val currentHistory = getAllHistory().toMutableList()

        // 2. 새 기록을 맨 앞에 추가
        currentHistory.add(0, record)

        // 3. 50개가 넘으면 가장 오래된 기록 삭제 (React 코드 로직 유지)
        if (currentHistory.size > 50) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        // 4. 리스트를 다시 JSON 문자열로 변환하여 저장
        val jsonString = gson.toJson(currentHistory)
        prefs.edit().putString("kioskLearningHistory", jsonString).apply()
    }
}