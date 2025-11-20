package com.example.kiosk.ui.screens.cafe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiosk.data.model.* // 기존에 정의된 데이터 모델들 import
import com.example.kiosk.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CafeKioskViewModel(application: Application) : AndroidViewModel(application) {

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

    private val _selectedCategory = MutableStateFlow("커피")
    val selectedCategory = _selectedCategory.asStateFlow()

    // 카페는 '세트 구성' 단계가 보통 없으므로 해당 변수들은 제거했습니다.

    fun setPracticeStep(step: Int) {
        _practiceStep.value = step
    }

    // === 1. 메뉴 및 옵션 데이터 정의 ===

    // 카페 전용 옵션
    private val iceAdjustmentOptions = listOf(
        ItemOption("얼음 추가"),
        ItemOption("얼음 적게"),
        ItemOption("얼음 빼기")
    )

    // [2] 커피 옵션 (HOT/ICE/샷 + 얼음조절)
    private val coffeeOptions = listOf(
        ItemOption("HOT"),
        ItemOption("ICE", 500),
        ItemOption("샷 추가", 500)
    ) + iceAdjustmentOptions

    // [3] 에이드/아이스티 옵션 (ICE Only + 얼음조절)
    private val adeOptions = listOf(
        ItemOption("ICE Only")
    ) + iceAdjustmentOptions

    // [4] 초코라떼 옵션 (HOT/ICE + 얼음조절)
    // (HOT 선택 시 얼음 옵션이 뜨긴 하겠지만, 일단 데이터는 이렇게 구성합니다)
    private val chocoOptions = listOf(
        ItemOption("HOT"),
        ItemOption("ICE", 500)
    ) + iceAdjustmentOptions // 👈 여기도 합체!
    val dessertOptions = listOf(
        ItemOption("기본", 0),
        ItemOption("포크 2개", 0)
    )

    val menuItems = listOf(
        // [커피]
        MenuItem("c1", "아메리카노", 2000, "커피", coffeeOptions),
        MenuItem("c2", "카페라떼", 3000, "커피", coffeeOptions),
        MenuItem("c3", "바닐라라떼", 3500, "커피", coffeeOptions),
        MenuItem("c4", "카페모카", 3800, "커피", coffeeOptions),

        MenuItem("d1", "레몬에이드", 3500, "음료", adeOptions),
        MenuItem("d2", "아이스티", 3500, "음료", adeOptions),
        MenuItem("d3", "초코라떼", 4500, "음료", chocoOptions),
        // [디저트]
        MenuItem("k1", "초코무스 케이크",  5500, "디저트", dessertOptions),
        MenuItem("k2", "치즈 케이크", 5500, "디저트", dessertOptions),
        MenuItem("k3", "크로플", 3500, "디저트", dessertOptions)
    )

    val categories = listOf("커피", "음료", "디저트")

    // === 2. 초기화 및 미션 설정 ===
    fun init(isPractice: Boolean) {
        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _practiceStep.value = if (isPractice) 0 else -1
        _selectedCategory.value = "커피"

        if (!isPractice) {
            val missions = listOf(
                Mission(
                    "아이스 아메리카노 2잔을 주문해보세요",
                    listOf(RequiredItem("아메리카노", 2, "ICE")) // 옵션 검증을 위해 필요한 경우 모델 수정 필요
                ),
                Mission(
                    "따뜻한 카페라떼 1잔, 치즈 케이크 1개를 주문해보세요",
                    listOf(RequiredItem("카페라떼", 1, "HOT"), RequiredItem("치즈 케이크", 1))
                ),
                Mission(
                    "레몬에이드 1잔, 초코무스 케이크 1개를 주문해보세요",
                    listOf(RequiredItem("레몬에이드", 1), RequiredItem("초코무스 케이크", 1))
                )
            )
            _currentMission.value = missions.random()
        } else {
            _currentMission.value = null
        }
    }

    // === 3. 연습 모드 단계 관리 ===
    fun startPractice() { _practiceStep.value = 1 }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        // 연습 모드 1단계(카테고리 선택) 완료 시 2단계로 이동
        if (_currentMission.value == null && _practiceStep.value == 1) _practiceStep.value = 2
    }

    // === 4. 장바구니 로직 ===
    fun addToCart(item: MenuItem, isPractice: Boolean, options: List<ItemOption> = emptyList(), quantity: Int = 1) {
        val currentCart = _cart.value.toMutableList()

        // 이미 장바구니에 같은 메뉴+옵션이 있는지 확인
        val existing = currentCart.find { it.menuItem.id == item.id && it.selectedOptions == options }

        if (existing != null) {
            // ✅ 기존 수량에 1을 더하는 게 아니라, 선택한 quantity만큼 더함
            existing.quantity += quantity
        } else {
            // ✅ 생성할 때: 기존 option은 null, 새 리스트에는 options 전달
            currentCart.add(
                CartItem(
                    menuItem = item,
                    quantity = quantity,
                    selectedOption = null, // 버거용은 비워둠
                    selectedOptions = options // 카페용 리스트 사용
                )
            )
        }
        _cart.value = currentCart
        updateTotal()

        // 연습 모드 단계 업데이트
        if (_currentMission.value == null && _practiceStep.value == 2) _practiceStep.value = 3
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
        _totalPrice.value = _cart.value.sumOf { item ->
            val basePrice = item.menuItem.price

            val optionsPrice = item.selectedOptions.sumOf { it.price }

            (basePrice + optionsPrice) * item.quantity
        }
    }

    // === 5. 결제 및 미션 검증 ===
    fun checkout(isPractice: Boolean) {
        val mission = _currentMission.value
        if (!isPractice && mission != null) {
            val success = checkMissionSuccess(mission, _cart.value)
            _orderResult.value = if (success) "success" else "fail"
            saveHistory(mission, success)
        } else {
            _orderResult.value = "complete"
        }
        // 연습 모드 종료 단계
        if (isPractice && _practiceStep.value == 3) _practiceStep.value = 4
    }

    // ⚠️ 중요: 카페 미션 검증 (옵션 포함)
    private fun checkMissionSuccess(mission: Mission, cart: List<CartItem>): Boolean {
        // 1. 아이템 개수가 일치하는지 확인 (단순화된 로직)
        if (cart.sumOf { it.quantity } != mission.required.sumOf { it.quantity }) return false

        // 2. 각 필수 항목이 장바구니에 존재하는지 확인
        return mission.required.all { req ->
            val cartItem = cart.find {
                // 이름이 같고, (옵션 요구사항이 있다면 옵션 이름도 포함되어야 함)
                it.menuItem.name == req.name &&
                        (req.option == null || it.selectedOption?.name?.contains(req.option) == true)
            }
            cartItem != null && cartItem.quantity == req.quantity
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

    // 현재 화면 카테고리 목록 반환
    fun getCurrentCategories() = categories

    // 현재 화면 메뉴 목록 반환
    fun getCurrentMenuItems() = menuItems

    fun clearCart() {
        _cart.value = emptyList()
        _totalPrice.value = 0
    }
}