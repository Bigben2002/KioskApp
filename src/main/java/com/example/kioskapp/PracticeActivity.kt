package com.example.kioskapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PracticeActivity : AppCompatActivity() {

    private lateinit var stepText: TextView
    private lateinit var btnNext: Button
    private var step = 0

    private val steps = arrayOf(
        "① 시작하기 버튼을 눌러주세요",
        "② 카테고리를 선택해보세요 (버거, 사이드, 음료)",
        "③ 메뉴를 눌러 장바구니에 담아보세요",
        "④ 장바구니를 열고 결제하기를 눌러보세요"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)

        stepText = findViewById(R.id.stepText)
        btnNext = findViewById(R.id.btnNext)

        stepText.text = steps[0]

        btnNext.setOnClickListener {
            step++
            if (step >= steps.size) {
                stepText.text = "연습이 완료되었습니다!"
                btnNext.isEnabled = false
                btnNext.text = "완료"
            } else {
                stepText.text = steps[step]
            }
        }
    }
}
