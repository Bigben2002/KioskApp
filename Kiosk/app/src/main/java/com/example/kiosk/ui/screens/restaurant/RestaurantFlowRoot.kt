package com.example.kiosk.ui.screens.restaurant

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun RestaurantFlowRoot(
    isPracticeMode: Boolean,
    onExit: () -> Unit,
    viewModel: RestaurantKioskViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()

    // 로컬 상태로 결제 플로우 관리 (카페 방식)
    var paymentStep by remember { mutableStateOf("MENU") }
    var selectedPaymentMethod by remember { mutableStateOf("") }

    // 초기화
    LaunchedEffect(Unit) {
        viewModel.init(isPracticeMode)
    }

    android.util.Log.d("RestaurantFlow", "Recompose - paymentStep: $paymentStep, orderResult: $orderResult")

    // 주문 완료 화면 (실전 모드에서만 표시)
    if (orderResult != null) {
        RestaurantOrderResultScreen(
            result = orderResult!!,
            cart = cart,
            totalPrice = totalPrice,
            mission = currentMission,
            viewModel = viewModel,
            onExit = onExit
        )
        return
    }

    // 결제 플로우 화면들
    when (paymentStep) {
        "PAY_METHOD" -> {
            RestaurantPaymentMethodSelectScreen(
                onPaid = { method ->
                    selectedPaymentMethod = method
                    paymentStep = "PAY_PROCESS"
                },
                onBack = { paymentStep = "MENU" }
            )
            return
        }
        "PAY_PROCESS" -> {
            var isProcessing by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(2000)
                isProcessing = true
                delay(2000)
                paymentStep = "PAY_SUCCESS"
            }
            if (isProcessing) {
                RestaurantPaymentProcessingScreen()
            } else {
                if (selectedPaymentMethod == "CARD") {
                    RestaurantPaymentCardInsertScreen()
                } else {
                    RestaurantPaymentQrScanScreen()
                }
            }
            return
        }
        "PAY_SUCCESS" -> {
            RestaurantPaymentSuccessScreen(
                cart = cart,
                totalPrice = totalPrice,
                isPracticeMode = isPracticeMode,
                onDone = {
                    viewModel.checkout(isPracticeMode)

                    // 카페와 동일한 로직
                    if (isPracticeMode) {
                        onExit() // 연습 모드 -> 바로 홈으로
                    } else {
                        paymentStep = "MENU" // 실전 모드 -> 결과 화면 표시
                    }
                }
            )
            return
        }
    }

    // 메인 키오스크 화면 (paymentStep == "MENU"일 때만)
    RestaurantKioskScreen(
        isPractice = isPracticeMode,
        onBack = onExit,
        viewModel = viewModel,
        onStartPayment = {
            android.util.Log.d("RestaurantFlow", "onStartPayment 호출됨!")
            paymentStep = "PAY_METHOD"
        }
    )
}