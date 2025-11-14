// app/src/main/java/com/example/kiosk/ui/screens/cinema/CinemaFoodScreen.kt
package com.example.kiosk.ui.screens.cinema

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.data.model.ItemOption
import com.example.kiosk.data.model.MenuItem
import kotlinx.coroutines.delay // ✅ 'delay' 임포트 추가

@Composable
fun CinemaFoodScreen(
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val categories = listOf("스낵", "음료", "세트")
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    val allItems = remember {
        listOf(
            MenuItem("sn1", "팝콘(S)", 4000, "스낵", options = listOf(ItemOption("기본", 0))),
            MenuItem("sn2", "팝콘(M)", 5500, "스낵"),
            MenuItem("sn3", "팝콘(L)", 7000, "스낵"),
            MenuItem("sn4", "나쵸", 5000, "스낵"),
            MenuItem("sn5", "핫도그", 4500, "스낵"),
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
        val f = allItems.filter { it.category == selectedCategory }
        if (f.isEmpty()) allItems else f
    }

    var cart by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var showCartDialog by remember { mutableStateOf(false) }

    // --- 결제 단계 상태 ---
    var step by remember { mutableStateOf(FoodStep.MENU) }
    var paymentStep by remember { mutableStateOf(PaymentStep.METHOD_SELECT) }

    val totalPrice by derivedStateOf {
        var sum = 0
        for (c in cart) {
            sum += c.menuItem.price * c.quantity
            if (c.selectedOption != null) sum += c.selectedOption!!.price * c.quantity
        }
        sum
    }
    val totalCount by derivedStateOf {
        var cnt = 0
        for (c in cart) cnt += c.quantity
        cnt
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
    }
    val onInc = { idx: Int ->
        val list = cart.toMutableList()
        if (idx in list.indices) list[idx] = list[idx].copy(quantity = list[idx].quantity + 1)
        cart = list
    }
    val onDec = { idx: Int ->
        val list = cart.toMutableList()
        if (idx in list.indices) {
            val q = list[idx].quantity - 1
            if (q <= 0) list.removeAt(idx) else list[idx] = list[idx].copy(quantity = q)
        }
        cart = list
    }
    val onClear = { cart = emptyList() }
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
                        onPaid = { paymentStep = PaymentStep.CARD_INSERT },
                        onBack = { step = FoodStep.MENU }
                    )
                }
                PaymentStep.CARD_INSERT -> {
                    PaymentCardInsertScreen()
                    LaunchedEffect(Unit) {
                        delay(2000)
                        paymentStep = PaymentStep.PROCESSING
                    }
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
                            cart = emptyList()
                            paymentStep = PaymentStep.METHOD_SELECT
                            step = FoodStep.MENU
                        },
                        onAgain = {
                            cart = emptyList()
                            paymentStep = PaymentStep.METHOD_SELECT
                            step = FoodStep.MENU
                        }
                    )
                }
            }
        }
    }
}