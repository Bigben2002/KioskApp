package com.example.kiosk.ui.screens.cinema

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.data.model.ItemOption
import com.example.kiosk.data.model.MenuItem
import com.example.kiosk.data.model.RequiredItem
import kotlinx.coroutines.delay

@Composable
fun CinemaFoodScreen(
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    // KioskViewModel 없이 독립적으로 작동
    foodCartState: List<CartItem> = emptyList(),
    onCartUpdate: (List<CartItem>) -> Unit = {},
    onPaymentSuccess: () -> Unit = {},
    missionRequiredFood: List<RequiredItem> = emptyList() // 미션은 없지만 UI는 유지
) {
    val categories = listOf("스낵", "음료", "세트")
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    val allItems = remember {
        listOf(
            MenuItem("sn1", "팝콘(S)", 4000, "스낵", options = listOf(ItemOption("기본", 0))),
            MenuItem("sn2", "팝콘(M)", 5500, "스낵"),
            MenuItem("sn3", "팝콘(L)", 7000, "스낵"),
            MenuItem("sn4", "나쵸", 5000, "스낵"),
            MenuItem("sn5", "핫도그", 4500, "스낵"), // 🌭
            MenuItem("dr1", "콜라(S)", 2500, "음료"),
            MenuItem("dr2", "콜라(M)", 3000, "음료"),
            MenuItem("dr3", "제로콜라", 3000, "음료"),
            MenuItem("dr4", "사이다", 3000, "음료"),
            MenuItem("st1", "팝콘L+콜라M 2", 9900, "세트"),
            MenuItem("st2", "팝콘M+콜라M", 7900, "세트"),
            MenuItem("st3", "나쵸+콜라M", 6900, "세트")
        )
    }

    val filtered = remember(selectedCategory) {
        allItems.filter { it.category == selectedCategory }
    }

    // 내부 상태를 관리하며 외부로 상태를 전달 (독립적인 작동)
    var cart by remember { mutableStateOf(if (onCartUpdate == {}) foodCartState else emptyList()) }
    LaunchedEffect(foodCartState) { if (onCartUpdate != {}) cart = foodCartState }

    var showCartDialog by remember { mutableStateOf(false) }

    // --- 결제 단계 상태 ---
    var step by remember { mutableStateOf(FoodStep.MENU) }
    var paymentStep by remember { mutableStateOf(PaymentStep.METHOD_SELECT) }

    val totalPrice by derivedStateOf {
        cart.sumOf { (it.menuItem.price + (it.selectedOption?.price ?: 0)) * it.quantity }
    }
    val totalCount by derivedStateOf {
        cart.sumOf { it.quantity }
    }

    // --- 카트 조작 함수 ---
    val onAdd = { item: MenuItem ->
        val list = cart.toMutableList()
        var found = false
        for (i in 0 until list.size) {
            if (list[i].menuItem.id == item.id) {
                list[i] = list[i].copy(quantity = list[i].quantity + 1)
                found = true; break
            }
        }
        if (!found) list.add(CartItem(item, 1, null))
        cart = list
        onCartUpdate(list)
    }
    val onInc = { idx: Int ->
        val list = cart.toMutableList()
        if (idx in list.indices) list[idx] = list[idx].copy(quantity = list[idx].quantity + 1)
        cart = list
        onCartUpdate(list)
    }
    val onDec = { idx: Int ->
        val list = cart.toMutableList()
        if (idx in list.indices) {
            val q = list[idx].quantity - 1
            if (q <= 0) list.removeAt(idx) else list[idx] = list[idx].copy(quantity = q)
        }
        cart = list
        onCartUpdate(list)
    }
    val onClear = {
        cart = emptyList()
        onCartUpdate(emptyList())
    }
    // --- ---

    // --- 화면 분기 (State Machine) ---
    when (step) {
        FoodStep.MENU -> {
            FoodMenuScreen(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
                items = filtered,
                onAdd = onAdd,
                totalCount = totalCount,
                totalPrice = totalPrice,
                onShowCart = { showCartDialog = true },
                missionRequiredFood = missionRequiredFood,
                modifier = modifier
            )

            if (showCartDialog) {
                CinemaCartDialog(
                    cart = cart,
                    totalPrice = totalPrice,
                    onDismiss = { showCartDialog = false },
                    onInc = onInc,
                    onDec = onDec,
                    onClear = onClear,
                    onCheckout = {
                        showCartDialog = false
                        if (cart.isNotEmpty()) step = FoodStep.PAYMENT
                    }
                )
            }
        }

        FoodStep.PAYMENT -> {
            when (paymentStep) {
                PaymentStep.METHOD_SELECT -> {
                    PaymentMethodSelectScreen(
                        onPaid = { method ->
                            if (method == "CARD") paymentStep = PaymentStep.CARD_INSERT
                            else if (method == "QR") paymentStep = PaymentStep.QR_SCAN
                        },
                        onBack = { step = FoodStep.MENU }
                    )
                }
                PaymentStep.CARD_INSERT -> {
                    PaymentCardInsertScreen()
                    LaunchedEffect(Unit) { delay(2000); paymentStep = PaymentStep.PROCESSING }
                }
                PaymentStep.QR_SCAN -> {
                    PaymentQrScanScreen()
                    LaunchedEffect(Unit) { delay(2000); paymentStep = PaymentStep.PROCESSING }
                }
                PaymentStep.PROCESSING -> {
                    PaymentProcessingScreen()
                    LaunchedEffect(Unit) {
                        delay(3000)
                        paymentStep = PaymentStep.SUCCESS
                    }
                }
                PaymentStep.SUCCESS -> {
                    FoodPaymentSuccessScreen(
                        cart = cart,
                        totalPrice = totalPrice,
                        onDone = {
                            onClose?.invoke()
                            onCartUpdate(emptyList())
                            paymentStep = PaymentStep.METHOD_SELECT
                            step = FoodStep.MENU
                        },
                        onAgain = {
                            onClose?.invoke()
                            onCartUpdate(emptyList())
                            paymentStep = PaymentStep.METHOD_SELECT
                            step = FoodStep.MENU
                        }
                    )
                }
            }
        }
    }
}