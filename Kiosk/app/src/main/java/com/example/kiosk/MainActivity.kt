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
import com.example.kiosk.ui.screens.burger.BurgerKioskScreen
import com.example.kiosk.ui.screens.cafe.CafeKioskScreen
import com.example.kiosk.ui.screens.cinema.CinemaFlowRoot
import com.example.kiosk.ui.screens.cinema.real.CinemaRealFlowRoot
import com.example.kiosk.ui.screens.main.MainMenuScreen
import com.example.kiosk.ui.screens.main.PracticeKioskSelectScreen
import com.example.kiosk.ui.screens.restaurant.RestaurantFlowRoot
import com.example.kiosk.ui.theme.KioskTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
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
                    modeIntent = ScreenState.PRACTICE
                    currentScreen = ScreenState.PRACTICE_SELECT
                },
                onNavigateToReal = {
                    modeIntent = ScreenState.REAL
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
            // 연습 모드 분기 (기존 코드 유지)
            when (currentKioskType) {
                KioskType.BURGER -> {
                    BurgerKioskScreen(
                        isPracticeMode = true,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CINEMA -> {
                    CinemaFlowRoot(
                        isPracticeMode = true,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CAFE -> {
                    CafeKioskScreen(
                        isPracticeMode = true,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.RESTAURANT -> {
                    RestaurantFlowRoot(
                        isPracticeMode = true,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                else -> {
                    KioskSimulatorScreen(
                        isPracticeMode = true,
                        kioskType = currentKioskType,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
            }
        }

        ScreenState.REAL -> {
            // 실전 모드 분기
            when (currentKioskType) {
                KioskType.BURGER -> {
                    BurgerKioskScreen(
                        isPracticeMode = false,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CINEMA -> {
                    CinemaRealFlowRoot(
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.CAFE -> {
                    CafeKioskScreen(
                        isPracticeMode = false,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                KioskType.RESTAURANT -> {
                    RestaurantFlowRoot(
                        isPracticeMode = false,
                        onExit = { currentScreen = ScreenState.MENU }
                    )
                }
                else -> {
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