package com.example.kiosk.ui.screens.restaurant

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RestaurantFlowRoot(
    isPracticeMode: Boolean,
    onExit: () -> Unit,
    viewModel: RestaurantKioskViewModel = viewModel()
) {
    val paymentStep by viewModel.paymentStep.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()

    // 주문 완료 화면
    if (orderResult != null) {
        RestaurantOrderResultScreen(
            result = orderResult!!,
            cart = viewModel.cart.collectAsState().value,
            totalPrice = viewModel.totalPrice.collectAsState().value,
            mission = viewModel.currentMission.collectAsState().value,
            onExit = onExit
        )
        return
    }

    // 결제 플로우 화면들
    when (paymentStep) {
        PaymentStep.METHOD_SELECT -> {
            RestaurantPaymentMethodSelectScreen(
                onPaid = { method -> viewModel.selectPaymentMethod(method) },
                onBack = { viewModel.cancelPayment() }
            )
            return
        }
        PaymentStep.CARD_INSERT -> {
            RestaurantPaymentCardInsertScreen(
                onProceed = { viewModel.proceedToProcessing() }
            )
            return
        }
        PaymentStep.QR_SCAN -> {
            RestaurantPaymentQrScanScreen(
                onProceed = { viewModel.proceedToProcessing() }
            )
            return
        }
        PaymentStep.PROCESSING -> {
            RestaurantPaymentProcessingScreen()
            return
        }
        PaymentStep.COMPLETE -> {
            LaunchedEffect(Unit) {
                viewModel.checkout(isPracticeMode)
            }
            return
        }
        else -> {
            // 일반 키오스크 화면
        }
    }

    // 메인 키오스크 화면
    RestaurantKioskScreen(
        isPractice = isPracticeMode,
        onBack = onExit,
        viewModel = viewModel
    )
}