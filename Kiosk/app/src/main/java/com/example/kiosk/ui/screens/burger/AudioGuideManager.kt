package com.example.kiosk.ui.screens.burger

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.*

class AudioGuideManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentStep: Int = -1

    // 연습 단계별 오디오 파일 매핑 (practiceStep과 매칭)
    private val stepAudioMap = mapOf(
        0 to "01_안녕하세요_환영합니다",                    // 시작 화면
        1 to "02_잘_하셨어요_이제_화면을",                  // 카테고리 선택
        2 to "03_잘_하셨어요_이제_오른쪽을",                // 메뉴 선택
        3 to "08_원하시는_만큼_고르셨다면",                 // 장바구니 확인 및 결제
        4 to "14_다_끝났어요_정말",                         // 완료
        5 to "04_세트로_주문하시는",                        // 세트 - 사이드 선택
        6 to "05_좋아요_이제_음료를",                       // 세트 - 음료 선택
        7 to "06_완벽해요_세트_구성이",                     // 세트 완료
        8 to "07_주문하신_내용을_확인해",                   // 장바구니 다이얼로그
        9 to "09_혹시_추가로_더",                          // 추천 메뉴
        10 to "10_결제를_할_거예요_어떤",                  // 결제 방식 선택
        11 to "11_카드를_기계에_꽂아주세요",               // 카드 결제
        12 to "12_큐알_코드를_화면에_비춰주세요",          // QR 결제
        13 to "13_잠시만_기다려주세요"                     // 결제 처리 중
    )

    fun playAudioForStep(step: Int) {
        // 같은 단계면 재생하지 않음 (중복 방지)
        if (currentStep == step) return
        currentStep = step

        val audioFileName = stepAudioMap[step] ?: return
        playAudio(audioFileName)
    }

    // 특정 이벤트에서 직접 재생 (단계와 무관)
    fun playAudioForEvent(audioFileName: String) {
        playAudio(audioFileName)
    }

    // 현재 재생 중인지 확인
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    private fun playAudio(fileName: String) {
        // 기존 재생 중인 오디오 정지
        stopAudio()

        try {
            // assets 폴더에서 오디오 파일 로드
            val assetManager = context.assets
            val afd = assetManager.openFd("audio/$fileName.mp3")

            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                start()
            }

            afd.close()

            // 재생 완료 리스너
            mediaPlayer?.setOnCompletionListener {
                stopAudio()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    fun release() {
        stopAudio()
    }
}

// Composable에서 사용할 수 있는 래퍼
@Composable
fun rememberAudioGuideManager(context: Context): AudioGuideManager {
    val manager = remember { AudioGuideManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            manager.release()
        }
    }

    return manager
}