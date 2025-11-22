package com.example.kiosk.ui.screens.restaurant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiosk.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 모델 클래스 정의
data class MenuItem(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Int,
    val category: String,
    val options: List<ItemOption>
)

data class ItemOption(
    val id: String,
    val name: String,
    val priceDelta: Int
)

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int,
    val option: ItemOption? = null
)

data class RequiredItem(val name: String, val quantity: Int)

data class Mission(val description: String, val required: List<RequiredItem>)

data class HistoryRecord(
    val id: String,
    val date: String,
    val mission: String,
    val success: Boolean,
    val userOrder: List<RequiredItem>,
    val timestamp: Long
)

class RestaurantKioskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HistoryRepository(application)

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

    private val _selectedCategory = MutableStateFlow("국밥류")
    val selectedCategory = _selectedCategory.asStateFlow()

    fun setPracticeStep(step: Int) {
        _practiceStep.value = step
    }

    // 옵션 정의
    // 돼지국밥, 순대국밥만 특(+1000원) 옵션 가능
    val specialOptions = listOf(
        ItemOption("normal", "보통", 0),
        ItemOption("special", "특 (+1,000원)", 1000)
    )

    // 모든 국밥에 수육 추가 옵션 가능
    val porkOptions = listOf(
        ItemOption("no_pork", "수육 없음", 0),
        ItemOption("add_pork", "수육 추가 (+5,000원)", 5000)
    )

    // 메뉴 데이터
    val menuItems = listOf(
        // 국밥류 (돼지국밥, 순대국밥만 특 옵션 가능)
        MenuItem("1", "돼지국밥", "부드러운 돼지고기가 가득", 9000, "국밥류", specialOptions),
        MenuItem("2", "순대국밥", "고소한 순대와 국밥", 9000, "국밥류", specialOptions),
        MenuItem("3", "내장국밥", "신선한 내장이 들어간", 10000, "국밥류", emptyList()),
        MenuItem("4", "섞어국밥", "돼지고기와 순대, 내장 모두", 11000, "국밥류", emptyList()),
        MenuItem("5", "뼈해장국", "얼큰한 뼈다귀 해장국", 10000, "국밥류", emptyList()),

        // 사이드
        MenuItem("11", "순대 모듬", "신선한 순대 한 접시", 15000, "사이드", emptyList()),
        MenuItem("12", "수육 (小)", "부드러운 수육 소", 15000, "사이드", emptyList()),
        MenuItem("13", "수육 (中)", "부드러운 수육 중", 20000, "사이드", emptyList()),
        MenuItem("14", "수육 (大)", "부드러운 수육 대", 25000, "사이드", emptyList()),
        MenuItem("15", "모듬", "순대+수육 모듬", 20000, "사이드", emptyList()),
        MenuItem("16", "김치", "직접 담근 김치", 3000, "사이드", emptyList()),

        // 음료
        MenuItem("21", "소주", "참이슬", 4000, "음료", emptyList()),
        MenuItem("22", "맥주", "시원한 생맥주", 4500, "음료", emptyList()),
        MenuItem("23", "콜라", "코카콜라", 2000, "음료", emptyList()),
        MenuItem("24", "사이다", "칠성사이다", 2000, "음료", emptyList()),
        MenuItem("25", "탄산수", "탄산수", 1500, "음료", emptyList())
    )

    val categories = listOf("국밥류", "사이드", "음료")

    fun init(isPractice: Boolean) {
        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _practiceStep.value = if (isPractice) 0 else -1
        _selectedCategory.value = "국밥류"

        if (!isPractice) {
            // 실전 모드 미션들
            val missions = listOf(
                Mission("돼지국밥 2개, 수육(소) 1개를 주문해보세요",
                    listOf(RequiredItem("돼지국밥", 2), RequiredItem("수육 (小)", 1))),
                Mission("순대국밥 1개, 소주 2병을 주문해보세요",
                    listOf(RequiredItem("순대국밥", 1), RequiredItem("소주", 2))),
                Mission("섞어국밥 1개, 순대 모듬 1개, 맥주 1병을 주문해보세요",
                    listOf(RequiredItem("섞어국밥", 1), RequiredItem("순대 모듬", 1), RequiredItem("맥주", 1))),
                Mission("내장국밥 2개, 김치 1개를 주문해보세요",
                    listOf(RequiredItem("내장국밥", 2), RequiredItem("김치", 1)))
            )
            _currentMission.value = missions.random()
        } else {
            _currentMission.value = null
        }
    }

    fun startPractice() {
        _practiceStep.value = 1
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (_currentMission.value == null && _practiceStep.value == 1) {
            _practiceStep.value = 2
        }
    }

    fun addToCart(item: MenuItem, isPractice: Boolean, option: ItemOption? = null, porkOption: ItemOption? = null) {
        val currentCart = _cart.value.toMutableList()

        // 옵션을 포함하여 동일한 아이템 찾기
        val existing = currentCart.find {
            it.menuItem.id == item.id && it.option == option
        }

        if (existing != null) {
            existing.quantity += 1
        } else {
            currentCart.add(CartItem(item, 1, option = option))
        }

        // 수육 추가 옵션이 선택된 경우
        if (porkOption != null && porkOption.id == "add_pork") {
            val porkItem = menuItems.find { it.id == "12" } // 수육 (小)
            if (porkItem != null) {
                val existingPork = currentCart.find { it.menuItem.id == "12" }
                if (existingPork != null) {
                    existingPork.quantity += 1
                } else {
                    currentCart.add(CartItem(porkItem, 1))
                }
            }
        }

        _cart.value = currentCart
        updateTotal()

        if (_currentMission.value == null && _practiceStep.value == 2) {
            _practiceStep.value = 3
        }
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
        _totalPrice.value = _cart.value.sumOf {
            val basePrice = it.menuItem.price
            val optionPrice = it.option?.priceDelta ?: 0
            (basePrice + optionPrice) * it.quantity
        }
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
        if (isPractice && _practiceStep.value == 3) {
            _practiceStep.value = 4
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
        val record = com.example.kiosk.data.model.HistoryRecord(
            id = System.currentTimeMillis().toString(),
            date = dateFormat.format(Date()),
            mission = mission.description,
            success = success,
            userOrder = _cart.value.map {
                com.example.kiosk.data.model.RequiredItem(it.menuItem.name, it.quantity)
            },
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveHistory(record)
        }
    }
}