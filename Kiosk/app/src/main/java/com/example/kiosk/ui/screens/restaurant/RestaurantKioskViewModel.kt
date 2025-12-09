package com.example.kiosk.ui.screens.restaurant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiosk.data.repository.HistoryRepository
import com.example.kiosk.data.model.MenuItem
import com.example.kiosk.data.model.ItemOption
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.data.model.Mission
import com.example.kiosk.data.model.RequiredItem
import com.example.kiosk.data.model.HistoryRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

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

    fun clearOrderResult() {
        _orderResult.value = null
    }

    val specialOptions = listOf(
        ItemOption("보통", 0),
        ItemOption("특 (+1,000원)", 1000)
    )

    val porkOptions = listOf(
        ItemOption("수육 없음", 0),
        ItemOption("수육 추가 (+5,000원)", 5000)
    )

    val menuItems = listOf(
        // 국밥류 (돼지국밥, 순대국밥만 특 옵션 가능)
        MenuItem("1", "돼지국밥", 9000, "국밥류", specialOptions),
        MenuItem("2", "순대국밥", 9000, "국밥류", specialOptions),
        MenuItem("3", "뚝배기불고기", 10000, "국밥류", emptyList()),
        MenuItem("4", "육개장", 11000, "국밥류", emptyList()),
        MenuItem("5", "뼈해장국", 10000, "국밥류", emptyList()),

        // 사이드
        MenuItem("11", "순대 모듬", 15000, "사이드", emptyList()),
        MenuItem("12", "수육 (小)", 15000, "사이드", emptyList()),
        MenuItem("13", "수육 (中)", 20000, "사이드", emptyList()),
        MenuItem("14", "수육 (大)", 25000, "사이드", emptyList()),
        MenuItem("15", "모듬", 20000, "사이드", emptyList()),
        MenuItem("16", "공기밥", 1000, "사이드", emptyList()),
        MenuItem("17", "김치", 0, "사이드", emptyList()),
        MenuItem("18", "물", 0, "사이드", emptyList()),

        // 음료
        MenuItem("21", "소주", 4000, "음료", emptyList()),
        MenuItem("22", "맥주", 4500, "음료", emptyList()),
        MenuItem("23", "콜라", 2000, "음료", emptyList()),
        MenuItem("24", "사이다", 2000, "음료", emptyList()),
        MenuItem("25", "탄산수", 1500, "음료", emptyList())
    )

    val categories = listOf("국밥류", "사이드", "음료")

    fun init(isPractice: Boolean) {
        android.util.Log.e("RESTAURANT_VM", "========== ViewModel init 시작! ==========")
        android.util.Log.e("RESTAURANT_VM", "isPractice: $isPractice")

        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _practiceStep.value = if (isPractice) 0 else -1
        _selectedCategory.value = "국밥류"

        android.util.Log.e("RESTAURANT_VM", "메뉴 개수: ${menuItems.size}")
        android.util.Log.e("RESTAURANT_VM", "선택된 카테고리: ${_selectedCategory.value}")

        val filteredMenu = menuItems.filter { it.category == _selectedCategory.value }
        android.util.Log.e("RESTAURANT_VM", "필터된 메뉴 개수: ${filteredMenu.size}")
        filteredMenu.forEach {
            android.util.Log.e("RESTAURANT_VM", "메뉴: ${it.name}, 카테고리: ${it.category}")
        }

        if (!isPractice) {
            val missions = listOf(
                Mission("돼지국밥 2개, 수육(소) 1개를 주문해보세요",
                    listOf(RequiredItem("돼지국밥", 2), RequiredItem("수육 (小)", 1))),
                Mission("순대국밥 1개, 소주 2병을 주문해보세요",
                    listOf(RequiredItem("순대국밥", 1), RequiredItem("소주", 2))),
                Mission("뚝배기불고기 1개, 순대 모듬 1개, 맥주 1병을 주문해보세요",
                    listOf(RequiredItem("뚝배기불고기", 1), RequiredItem("순대 모듬", 1), RequiredItem("맥주", 1))),
                Mission("육개장 2개, 공기밥 1개를 주문해보세요",
                    listOf(RequiredItem("육개장", 2), RequiredItem("공기밥", 1))),
                Mission("뼈해장국 1개, 공기밥 2개, 콜라 1개를 주문해보세요",
                    listOf(RequiredItem("뼈해장국", 1), RequiredItem("공기밥", 2), RequiredItem("콜라", 1))),
                Mission("순대국밥 2개, 순대 모듬 1개를 주문해보세요",
                    listOf(RequiredItem("순대국밥", 2), RequiredItem("순대 모듬", 1))),
                Mission("돼지국밥 1개, 수육 (中) 1개, 소주 1병을 주문해보세요",
                    listOf(RequiredItem("돼지국밥", 1), RequiredItem("수육 (中)", 1), RequiredItem("소주", 1))),
                Mission("뚝배기불고기 2개, 사이다 2개를 주문해보세요",
                    listOf(RequiredItem("뚝배기불고기", 2), RequiredItem("사이다", 2))),
                Mission("육개장 1개, 모듬 1개, 맥주 2병을 주문해보세요",
                    listOf(RequiredItem("육개장", 1), RequiredItem("모듬", 1), RequiredItem("맥주", 2))),
                Mission("돼지국밥(특) 1개를 주문해보세요",
                    listOf(RequiredItem("돼지국밥", 1, option = "특"))),
                Mission("순대국밥(특) 1개, 공기밥 1개를 주문해보세요",
                    listOf(RequiredItem("순대국밥", 1, option = "특"), RequiredItem("공기밥", 1))),
                Mission("돼지국밥(수육 추가) 1개를 주문해보세요",
                    listOf(RequiredItem("돼지국밥", 1, option = "수육 추가"))),
                Mission("순대국밥(특, 수육 추가) 1개, 맥주 1병을 주문해보세요",
                    listOf(RequiredItem("순대국밥", 1, option = "특, 수육 추가"), RequiredItem("맥주", 1)))
            )
            _currentMission.value = missions.random()
        } else {
            _currentMission.value = null
        }

        android.util.Log.e("RESTAURANT_VM", "init 완료!")
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

        // 수육 추가 옵션이 선택된 경우, 옵션들을 결합
        val finalOption = if (porkOption != null && porkOption.name.contains("수육 추가")) {
            // 기본 옵션과 수육 추가 옵션을 결합
            val optionName = if (option != null && option.price > 0) {
                "${option.name}, ${porkOption.name}"
            } else if (option != null) {
                porkOption.name  // "보통"인 경우 수육 추가만 표시
            } else {
                porkOption.name
            }
            val optionPrice = (option?.price ?: 0) + porkOption.price
            ItemOption(optionName, optionPrice)
        } else {
            option
        }

        val existing = currentCart.find {
            it.menuItem.id == item.id && it.selectedOption == finalOption
        }

        if (existing != null) {
            existing.quantity += 1
        } else {
            currentCart.add(CartItem(item, 1, selectedOption = finalOption))
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
            val optionPrice = it.selectedOption?.price ?: 0
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
    }

    fun reset() {
        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _currentMission.value = null
        _practiceStep.value = 0
        _selectedCategory.value = "국밥류"
    }

    private fun checkMissionSuccess(mission: Mission, cart: List<CartItem>): Boolean {
        val cartTotal = cart.sumOf { it.quantity }
        val missionTotal = mission.required.sumOf { it.quantity }
        if (cartTotal != missionTotal) {
            android.util.Log.d("RESTAURANT_MISSION", "❌ 전체 개수 불일치: 장바구니 $cartTotal, 미션 $missionTotal")
            return false
        }

        return mission.required.all { req ->
            android.util.Log.d("RESTAURANT_MISSION", "🔎 미션 확인: ${req.name} x${req.quantity}, 옵션: ${req.option}")

            val matchingQuantity = cart.filter { item ->
                val nameMatch = item.menuItem.name == req.name

                val optionMatch = if (req.option == null) {
                    true
                } else {
                    val requiredOptions = req.option.split(",").map { it.trim() }
                    val cartOptionName = item.selectedOption?.name ?: ""

                    val allOptionsMatch = requiredOptions.all { requiredOpt ->
                        cartOptionName.contains(requiredOpt)
                    }

                    android.util.Log.d("RESTAURANT_MISSION",
                        "   - 필수 옵션: $requiredOptions, 장바구니 옵션: $cartOptionName, 일치: $allOptionsMatch")

                    allOptionsMatch
                }

                val isMatch = nameMatch && optionMatch

                if (nameMatch) {
                    android.util.Log.d("RESTAURANT_MISSION",
                        "   - 장바구니: ${item.menuItem.name}, 옵션: ${item.selectedOption?.name} → 일치: $isMatch")
                }

                isMatch
            }.sumOf { it.quantity }

            val success = matchingQuantity == req.quantity
            android.util.Log.d("RESTAURANT_MISSION", "   👉 수량: $matchingQuantity / 필요: ${req.quantity} → $success")
            success
        }
    }

    private fun saveHistory(mission: Mission, success: Boolean) {
        val dateFormat = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
        val record = com.example.kiosk.data.model.HistoryRecord(
            id = System.currentTimeMillis().toString(),
            date = dateFormat.format(Date()),
            mission = mission.text,
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