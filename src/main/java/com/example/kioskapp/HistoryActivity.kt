package com.example.kioskapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val tvHistory = findViewById<TextView>(R.id.tvHistory)
        tvHistory.text = """
            🧾 학습 기록 예시
            
            • 2025-11-08 실전모드 성공
            • 2025-11-07 실전모드 실패
            • 2025-11-06 연습모드 완료
        """.trimIndent()
    }
}
