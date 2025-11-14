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

    when (currentScreen) {
        ScreenState.MENU -> {
            MainMenuScreen(
                onNavigateToPractice = {
                    // 이전: PRACTICE 로 바로 진입
                    // 변경: 선택 페이지로
                    currentScreen = ScreenState.PRACTICE_SELECT
                },
                onNavigateToReal = {
                    // 실전 모드는 기존 흐름 유지(원하면 선택 페이지로 확장 가능)
                    currentScreen = ScreenState.REAL
                },
                onOpenHelp = { showHelpDialog = true },
                onOpenHistory = { showHistoryDialog = true }
            )
        }

        ScreenState.PRACTICE_SELECT -> {
            PracticeKioskSelectScreen(
                onSelect = { type ->
                    currentKioskType = type
                    // BURGER/CAFE/CINEMA 공통으로 PRACTICE 진입
                    currentScreen = ScreenState.PRACTICE
                },
                onBack = { currentScreen = ScreenState.MENU }
            )
        }

        ScreenState.PRACTICE -> {
            // BURGER/CAFE는 기존 시뮬레이터 사용, CINEMA는 루트에서 처리
            if (currentKioskType == KioskType.CINEMA) {
                CinemaFlowRoot(
                    isPracticeMode = true,
                    onExit = { currentScreen = ScreenState.MENU }
                )
            } else {
                KioskSimulatorScreen(
                    isPracticeMode = true,
                    kioskType = currentKioskType,
                    onExit = { currentScreen = ScreenState.MENU }
                )
            }
        }

        ScreenState.REAL -> {
            // REAL에서도 영화관을 선택한 상태라면(나중 확장용), 루트로 보낼 수 있음.
            if (currentKioskType == KioskType.CINEMA) {
                CinemaFlowRoot(
                    isPracticeMode = false,
                    onExit = { currentScreen = ScreenState.MENU }
                )
            } else {
                KioskSimulatorScreen(
                    isPracticeMode = false,
                    kioskType = currentKioskType,
                    onExit = { currentScreen = ScreenState.MENU }
                )
            }
        }
    }

    if (showHelpDialog) HelpDialog(onDismiss = { showHelpDialog = false })
    if (showHistoryDialog) LearningHistoryDialog(onDismiss = { showHistoryDialog = false })
}
