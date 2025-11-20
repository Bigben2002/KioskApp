package com.example.kiosk
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kiosk.data.model.KioskType
import com.example.kiosk.ui.components.HelpDialog
import com.example.kiosk.ui.components.LearningHistoryDialog
import com.example.kiosk.ui.screens.KioskSimulatorScreen
import com.example.kiosk.ui.screens.burger.BurgerKioskScreen  // ✨ 새로 추가
import com.example.kiosk.ui.screens.cafe.CafeKioskScreen
import com.example.kiosk.ui.screens.cinema.CinemaFlowRoot
import com.example.kiosk.ui.screens.main.MainMenuScreen
import com.example.kiosk.ui.screens.main.PracticeKioskSelectScreen
import com.example.kiosk.ui.theme.KioskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KioskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KioskApp()
                }
            }
        }
    }
}

// 화면 상태
enum class ScreenState {
    MENU, PRACTICE_SELECT, PRACTICE, REAL
}

@Composable
fun KioskApp() {
    var currentScreen by remember { mutableStateOf(ScreenState.MENU) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    // 현재 선택된 매장 타입 (선택 페이지에서 설정)
    var currentKioskType by remember { mutableStateOf(KioskType.BURGER) }
    var modeIntent by remember { mutableStateOf(ScreenState.PRACTICE) }

    when (currentScreen) {
        ScreenState.MENU -> {
            MainMenuScreen(
                onNavigateToPractice = {
                    modeIntent = ScreenState.PRACTICE // 👈 인텐트 저장
                    currentScreen = ScreenState.PRACTICE_SELECT
                },
                onNavigateToReal = {
                    modeIntent = ScreenState.REAL // 👈 인텐트 저장
                    currentScreen = ScreenState.PRACTICE_SELECT
                },
                onOpenHelp = { showHelpDialog = true },
                onOpenHistory = { showHistoryDialog = true }
            )
        }

        ScreenState.PRACTICE_SELECT -> {
            PracticeKioskSelectScreen(
                onSelect = { type ->
                    currentKioskType = type
                    currentScreen = modeIntent
                },
                onBack = { currentScreen = ScreenState.MENU }
            )
        }

        ScreenState.PRACTICE -> {
            // ✨ 핵심 분기: 키오스크 타입에 따라 다른 화면으로 이동
            when (currentKioskType) {
                KioskType.BURGER -> {
                    // 🍔 새로운 버거 키오스크 (고급 기능)
                    BurgerKioskScreen(
                        isPracticeMode = true,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CINEMA -> {
                    // 🎬 영화관 전용 화면
                    CinemaFlowRoot(
                        isPracticeMode = true,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CAFE -> {
                    // ☕️ 새로운 카페 키오스크 (고급 기능)
                    CafeKioskScreen(
                        isPracticeMode = true,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                else -> {
                    // ☕ 카페, 식당 등은 기존 시뮬레이터 사용
                    KioskSimulatorScreen(
                        isPracticeMode = true,
                        kioskType = currentKioskType,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
            }
        }

        ScreenState.REAL -> {
            // 실전 모드도 동일하게 분기 처리
            when (currentKioskType) {
                KioskType.BURGER -> {
                    // 🍔 새로운 버거 키오스크 (미션 모드)
                    BurgerKioskScreen(
                        isPracticeMode = false,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CINEMA -> {
                    // 🎬 영화관 전용 화면
                    CinemaFlowRoot(
                        isPracticeMode = false,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CAFE -> {
                // ☕️ 새로운 카페 키오스크 (고급 기능)
                CafeKioskScreen(
                    isPracticeMode = false,
                    onExit = { currentScreen = ScreenState.MENU }
                )
            }
                else -> {
                    // ☕ 카페, 식당 등은 기존 시뮬레이터 사용
                    KioskSimulatorScreen(
                        isPracticeMode = false,
                        kioskType = currentKioskType,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
            }
        }
    }

    if (showHelpDialog) HelpDialog(onDismiss = { showHelpDialog = false })
    if (showHistoryDialog) LearningHistoryDialog(onDismiss = { showHistoryDialog = false })
}