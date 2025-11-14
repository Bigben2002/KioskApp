package com.example.kiosk.ui.screens

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

    // === 상태 변수들 ===
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice = _totalPrice.asStateFlow()

    private val _currentMission = MutableStateFlow<Mission?>(null)
    val currentMission = _currentMission.asStateFlow()

    private val _practiceStep = MutableStateFlow(0)
    val practiceStep = _practiceStep.asStateFlow()

    private val _orderResult = MutableStateFlow<String?>(null)
    val orderResult = _orderResult.asStateFlow()

    // 현재 선택된 키오스크 타입 (기본값은 버거)
    private var currentType: KioskType = KioskType.BURGER

    // === 1. 메뉴 데이터 분리 ===
    private val burgerItems = listOf(
        MenuItem("1", "불고기버거", 4500, "버거"),
        MenuItem("2", "치즈버거", 4000, "버거"),
        MenuItem("3", "새우버거", 5000, "버거"),
        MenuItem("4", "감자튀김", 2000, "사이드"),
        MenuItem("5", "치킨너겟", 3000, "사이드"),
        MenuItem("6", "콜라", 1500, "음료"),
        MenuItem("7", "사이다", 1500, "음료"),
        MenuItem("8", "아이스티", 2000, "음료")
    )

    private val cafeItems = listOf(
        MenuItem(
            "c1",
            "아메리카노",
            2000,
            "커피",
            options = listOf(ItemOption("HOT"), ItemOption("ICE", 500))
        ),
        MenuItem(
            "c2",
            "카페라떼",
            3000,
            "커피",
            options = listOf(ItemOption("HOT"), ItemOption("ICE", 500))
        ),
        MenuItem(
            "c3",
            "바닐라라떼",
            3500,
            "커피",
            options = listOf(ItemOption("HOT"), ItemOption("ICE", 500))
        ),
        MenuItem("c4", "레몬에이드", 3500, "음료"),
        MenuItem("c5", "초코케이크", 5500, "디저트"),
        MenuItem("c6", "치즈케이크", 5500, "디저트")
    )

    // === 2. 미션 데이터 분리 ===
    private val burgerMissions = listOf(
        Mission(
            "새우버거 3개, 콜라 1잔을 주문해보세요",
            listOf(RequiredItem("새우버거", 3), RequiredItem("콜라", 1))
        ),
        Mission(
            "불고기버거 2개, 감자튀김 1개를 주문해보세요",
            listOf(RequiredItem("불고기버거", 2), RequiredItem("감자튀김", 1))
        ),
        Mission(
            "치즈버거 1개, 사이다 2잔을 주문해보세요",
            listOf(RequiredItem("치즈버거", 1), RequiredItem("사이다", 2))
        )
    )

    private val cafeMissions = listOf(
        Mission(
            "아이스 아메리카노 2잔을 주문해보세요",
            listOf(RequiredItem("아메리카노", 2))
        ),
        Mission(
            "카페라떼 1잔, 초코케이크 1개를 주문해보세요",
            listOf(RequiredItem("카페라떼", 1), RequiredItem("초코케이크", 1))
        ),
        Mission(
            "레몬에이드 1잔, 치즈케이크 1개를 주문해보세요",
            listOf(RequiredItem("레몬에이드", 1), RequiredItem("치즈케이크", 1))
        )
    )

    // === 3. 초기화 함수 수정 (타입 전달받음) ===
    fun init(isPractice: Boolean, type: KioskType) {
        currentType = type
        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _practiceStep.value = if (isPractice) 0 else -1

        if (!isPractice) {
            val missions = when (type) {
                KioskType.BURGER -> burgerMissions
                KioskType.CAFE -> cafeMissions
                // ✅ 영화관/식당은 시연 중심(미션 없음)
                KioskType.CINEMA, KioskType.RESTAURANT -> emptyList()
            }
            _currentMission.value = missions.randomOrNull()
        } else {
            _currentMission.value = null
        }
    }

    fun getCurrentMenuItems(): List<MenuItem> {
        return when (currentType) {
            KioskType.BURGER -> burgerItems
            KioskType.CAFE -> cafeItems
            // ✅ 단계형 UI: 메뉴 없음
            KioskType.CINEMA, KioskType.RESTAURANT -> emptyList()
        }
    }

    fun getCurrentCategories(): List<String> {
        return currentType.categories
    }

    // 현재 설정(테마 색상 등)을 UI가 가져갈 수 있게 함
    fun getConfig(): KioskType = currentType

    // === 아래 로직들은 기존과 동일 ===
    fun startPractice() {
        _practiceStep.value = 1
    }

    fun selectCategory(isPractice: Boolean) {
        if (isPractice && _practiceStep.value == 1) _practiceStep.value = 2
    }

    fun addToCart(item: MenuItem, isPractice: Boolean, option: ItemOption? = null) {
        val currentCart = _cart.value.toMutableList()
        val existing = currentCart.find { it.menuItem.id == item.id && it.selectedOption == option }
        if (existing != null) {
            existing.quantity += 1
        } else {
            currentCart.add(CartItem(item, 1, selectedOption = option))
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
        _totalPrice.value =
            _cart.value.sumOf { (it.menuItem.price + (it.selectedOption?.price ?: 0)) * it.quantity }
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
        return mission.required.all { req ->
            val cartItem = cart.find { it.menuItem.name == req.name }
            cartItem != null && cartItem.quantity == req.quantity
        } && cart.size == mission.required.size
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
