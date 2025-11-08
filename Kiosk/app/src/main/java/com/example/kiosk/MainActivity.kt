package com.example.kiosk
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.kiosk.ui.components.HelpDialog
import com.example.kiosk.ui.components.LearningHistoryDialog
import com.example.kiosk.ui.screens.main.MainMenuScreen
import com.example.kiosk.ui.screens.simulator.KioskSimulatorScreen
import com.example.kiosk.ui.theme.KioskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 우리가 만든 커스텀 테마 적용
            KioskTheme {
                // A surface container using the 'background' color from the theme
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

// 화면 상태 정의 (메뉴, 연습 모드, 실전 모드)
enum class ScreenState {
    MENU, PRACTICE, REAL
}

@Composable
fun KioskApp() {
    // 현재 어떤 화면을 보여줄지 상태 관리
    var currentScreen by remember { mutableStateOf(ScreenState.MENU) }
    // 다이얼로그 표시 여부 상태 관리
    var showHelpDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    // currentScreen 상태에 따라 다른 화면 Composable 표시
    when (currentScreen) {
        ScreenState.MENU -> {
            MainMenuScreen(
                onNavigateToPractice = { currentScreen = ScreenState.PRACTICE },
                onNavigateToReal = { currentScreen = ScreenState.REAL },
                onOpenHelp = { showHelpDialog = true },
                onOpenHistory = { showHistoryDialog = true }
            )
        }
        ScreenState.PRACTICE -> {
            KioskSimulatorScreen(
                isPracticeMode = true,
                onExit = { currentScreen = ScreenState.MENU }
            )
        }
        ScreenState.REAL -> {
            KioskSimulatorScreen(
                isPracticeMode = false,
                onExit = { currentScreen = ScreenState.MENU }
            )
        }
    }

    // 도움말 다이얼로그가 true일 때 표시
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    // 학습 기록 다이얼로그가 true일 때 표시
    if (showHistoryDialog) {
        LearningHistoryDialog(onDismiss = { showHistoryDialog = false })
    }
}