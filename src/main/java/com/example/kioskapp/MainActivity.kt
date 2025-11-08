package com.example.kioskapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnPracticeStart: TextView
    private lateinit var btnRealStart: TextView
    private lateinit var btnHistory: TextView
    private lateinit var btnHowTo: TextView
    private lateinit var btnHelpIcon: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPracticeStart = findViewById(R.id.btnPracticeStart)
        btnRealStart = findViewById(R.id.btnRealStart)
        btnHistory = findViewById(R.id.btnHistory)
        btnHowTo = findViewById(R.id.btnHowTo)
        btnHelpIcon = findViewById(R.id.ivHelp)

        // 연습 모드 이동
        btnPracticeStart.setOnClickListener {
            startActivity(Intent(this, PracticeActivity::class.java))
        }

        // 실전 모드 이동
        btnRealStart.setOnClickListener {
            startActivity(Intent(this, RealModeActivity::class.java))
        }

        // 학습 기록 이동
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // 도움말 다이얼로그
        val helpListener = View.OnClickListener {
            val dialog = HelpDialog()
            dialog.show(supportFragmentManager, "help")
        }
        btnHowTo.setOnClickListener(helpListener)
        btnHelpIcon.setOnClickListener(helpListener)
    }
}
