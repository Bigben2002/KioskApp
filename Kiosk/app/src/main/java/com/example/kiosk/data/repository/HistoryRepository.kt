package com.example.kiosk.data.repository

import android.app.Application
import android.util.Log
import com.example.kiosk.data.model.HistoryRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HistoryRepository(application: Application) {

    // ✅ [안전 장치] 파이어베이스가 설정 안 돼있어도 앱이 안 죽게 함
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("HistoryRepository", "파이어베이스 초기화 실패 (앱은 계속 실행됨): ${e.message}")
            null
        }
    }

    private val collectionRef get() = db?.collection("kiosk_history")

    // 저장 함수
    suspend fun saveHistory(record: HistoryRecord) {
        // DB가 없으면 저장 안 하고 조용히 끝냄
        if (db == null) return

        try {
            collectionRef?.document(record.id)?.set(record)?.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 불러오기 함수
    suspend fun getAllHistory(): List<HistoryRecord> {
        if (db == null) return emptyList()

        return try {
            val snapshot = collectionRef
                ?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.get()
                ?.await()

            snapshot?.toObjects(HistoryRecord::class.java) ?: emptyList()
        } catch (e: Exception) {
            // 👇 [수정] 여기에 로그를 추가하세요!
            Log.e("HistoryRepository", "데이터 불러오기 실패 ㅠㅠ: ${e.message}")
            e.printStackTrace() // 에러 내용을 자세히 출력

            emptyList()
        }
    }
}