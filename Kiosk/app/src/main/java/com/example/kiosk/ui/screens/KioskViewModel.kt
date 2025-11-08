package com.example.kiosk.ui.screens.simulator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiosk.data.model.*
import com.example.kiosk.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class KioskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HistoryRepository(application)

    // 상태
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice = _totalPrice.asStateFlow()

    private val _currentMission = MutableStateFlow<Mission?>(null)
    val currentMission = _currentMission.asStateFlow()

    // 연습 모드 단계
    private val _practiceStep = MutableStateFlow(0)
    val practiceStep = _practiceStep.asStateFlow()

    // 주문 결과 (성공/실패/완료)
    private val _orderResult = MutableStateFlow<String?>(null)
    val orderResult = _orderResult.asStateFlow()

    // 메뉴 데이터 (상수로 정의)
    val menuItems = listOf(
        MenuItem("1", "불고기버거", 4500, "버거"),
        MenuItem("2", "치즈버거", 4000, "버거"),
        MenuItem("3", "새우버거", 5000, "버거"),
        MenuItem("4", "감자튀김", 2000, "사이드"),
        MenuItem("5", "치킨너겟", 3000, "사이드"),
        MenuItem("6", "콜라", 1500, "음료"),
        MenuItem("7", "사이다", 1500, "음료"),
        MenuItem("8", "아이스티", 2000, "음료")
    )
    val categories = listOf("버거", "사이드", "음료")

    fun init(isPractice: Boolean) {
        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _practiceStep.value = if (isPractice) 0 else -1

        if (!isPractice) {
            // 랜덤 미션 설정
            val missions = listOf(
                Mission("새우버거 3개, 콜라 1잔을 주문해보세요", listOf(RequiredItem("새우버거", 3), RequiredItem("콜라", 1))),
                Mission("불고기버거 2개, 감자튀김 1개를 주문해보세요", listOf(RequiredItem("불고기버거", 2), RequiredItem("감자튀김", 1))),
                // ... 더 많은 미션 추가 가능
            )
            _currentMission.value = missions.random()
        } else {
            _currentMission.value = null
        }
    }

    fun startPractice() { _practiceStep.value = 1 }
    fun selectCategory(isPractice: Boolean) { if (isPractice && _practiceStep.value == 1) _practiceStep.value = 2 }

    fun addToCart(item: MenuItem, isPractice: Boolean) {
        val currentCart = _cart.value.toMutableList()
        val existing = currentCart.find { it.menuItem.id == item.id }
        if (existing != null) {
            existing.quantity += 1
        } else {
            currentCart.add(CartItem(item, 1))
        }
        _cart.value = currentCart
        updateTotal()
        if (isPractice && _practiceStep.value == 2) _practiceStep.value = 3
    }

    fun updateQuantity(itemId: String, delta: Int) {
        _cart.value = _cart.value.mapNotNull {
            if (it.menuItem.id == itemId) {
                val newQty = it.quantity + delta
                if (newQty > 0) it.copy(quantity = newQty) else null
            } else it
        }
        updateTotal()
    }

    private fun updateTotal() {
        _totalPrice.value = _cart.value.sumOf { it.menuItem.price * it.quantity }
    }

    fun checkout(isPractice: Boolean) {
        val mission = _currentMission.value
        if (!isPractice && mission != null) {
            val success = checkMissionSuccess(mission, _cart.value)
            _orderResult.value = if (success) "success" else "fail"
            saveHistory(mission, success)
        } else {
            _orderResult.value = "complete"
        }
    }

    private fun checkMissionSuccess(mission: Mission, cart: List<CartItem>): Boolean {
        if (cart.size != mission.required.size) return false
        return mission.required.all { req ->
            cart.find { it.menuItem.name == req.name }?.quantity == req.quantity
        }
    }

    private fun saveHistory(mission: Mission, success: Boolean) {
        val dateFormat = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
        val record = HistoryRecord(
            id = System.currentTimeMillis().toString(),
            date = dateFormat.format(Date()),
            mission = mission.text,
            success = success,
            userOrder = _cart.value.map { RequiredItem(it.menuItem.name, it.quantity) },
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch { repository.saveHistory(record) }
    }
}