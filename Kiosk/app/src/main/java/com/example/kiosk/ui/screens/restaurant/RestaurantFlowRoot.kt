package com.example.kiosk.ui.screens.restaurant

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.example.kiosk.ui.screens.OrderResultScreen

@Composable
fun RestaurantFlowRoot(
    isPracticeMode: Boolean,
    onExit: () -> Unit
) {
    val sessionId = remember { System.currentTimeMillis().toString() }

    val viewModel: RestaurantKioskViewModel = viewModel(key = "restaurant_$sessionId")

    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()

    var paymentStep by remember { mutableStateOf("MENU") }
    var selectedPaymentMethod by remember { mutableStateOf("") }

    val isInitialized = remember { mutableStateOf(false) }

    SideEffect {
        if (!isInitialized.value) {
            android.util.Log.d("RestaurantFlow", "=== SideEffect: 첫 진입, 초기화 시작 ===")
            viewModel.clearOrderResult()
            isInitialized.value = true
        }
    }

    LaunchedEffect(Unit) {
        android.util.Log.d("RestaurantFlow", "=== LaunchedEffect: init 호출 ===")
        viewModel.init(isPracticeMode)
    }

    android.util.Log.d("RestaurantFlow", "Recompose - paymentStep: $paymentStep, orderResult: $orderResult")

    // 주문 완료 화면 (실전 모드에서만 표시)
    if (orderResult != null) {
        OrderResultScreen(
            result = orderResult!!,
            mission = currentMission,
            cart = cart,
            totalPrice = totalPrice,
            onExit = {
                viewModel.reset()
                onExit()
            }
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

                    if (isPracticeMode) {
                        onExit()
                    } else {
                        paymentStep = "MENU"
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