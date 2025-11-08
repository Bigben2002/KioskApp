package com.example.kioskapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class RealModeActivity : AppCompatActivity() {

    private lateinit var missionText: TextView
    private lateinit var btnResult: Button

    private val missions = arrayOf(
        "새우버거 3개, 콜라 1잔을 주문하세요",
        "불고기버거 2개, 감자튀김 1개를 주문하세요",
        "치즈버거 1개, 사이다 2잔을 주문하세요",
        "불고기버거 1개, 아이스티 1잔을 주문하세요"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realmode)

        missionText = findViewById(R.id.missionText)
        btnResult = findViewById(R.id.btnResult)

        missionText.text = missions[Random.nextInt(missions.size)]

        btnResult.setOnClickListener {
            missionText.text = "미션 완료! 수고하셨습니다 😊"
            btnResult.isEnabled = false
        }
    }
}
