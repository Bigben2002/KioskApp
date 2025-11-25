package com.example.kiosk.ui.screens.burger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiosk.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import com.example.kiosk.data.model.HistoryRecord as DataHistoryRecord
import com.example.kiosk.data.model.RequiredItem as DataRequiredItem

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
    val option: ItemOption? = null,
    val isSetComponent: Boolean = false
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

// 결제 플로우 상태
enum class PaymentStep {
    NONE,              // 결제 전
    METHOD_SELECT,     // 결제 방식 선택
    CARD_INSERT,       // 카드 삽입 대기
    QR_SCAN,          // QR 스캔 대기
    PROCESSING,       // 결제 처리 중
    COMPLETE          // 결제 완료
}

class BurgerKioskViewModel(application: Application) : AndroidViewModel(application) {

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
    private val _selectedCategory = MutableStateFlow("버거")
    val selectedCategory = _selectedCategory.asStateFlow()

    // 세트 주문 상태
    private val _isSelectingSetComponents = MutableStateFlow(false)
    val isSelectingSetComponents = _isSelectingSetComponents.asStateFlow()

    private val _currentSetBurger = MutableStateFlow<MenuItem?>(null)
    val currentSetBurger = _currentSetBurger.asStateFlow()

    // 결제 플로우 상태
    private val _paymentStep = MutableStateFlow(PaymentStep.NONE)
    val paymentStep = _paymentStep.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<String?>(null)
    val selectedPaymentMethod = _selectedPaymentMethod.asStateFlow()

    fun setPracticeStep(step: Int) {
        _practiceStep.value = step
    }

    // 메뉴 데이터
    val defaultOptions = listOf(
        ItemOption("basic", "단품", 0),
        ItemOption("set", "세트 (+3,500원)", 3500),
        ItemOption("size_up", "사이즈 업 (+1,000원)", 1000)
    )

    val menuItems = listOf(
        MenuItem("1", "불고기버거", "제일 싸고 제일 얇고", 3500, "버거", defaultOptions),
        MenuItem("2", "치즈버거", "솔직히 치즈 한장넣고 이러는거 좀 그래", 4000, "버거", defaultOptions),
        MenuItem("3", "새우버거", "버거킹 통새우와퍼 맛있던데", 5000, "버거", defaultOptions),
        MenuItem("4", "베이컨 토마토 디럭스", "무난하게 좋은", 6500, "버거", defaultOptions),
        MenuItem("5", "더블 불고기 버거", "패티 2장인데 큰 차이는 없는 것 같은", 5500, "버거", defaultOptions),

        MenuItem("11", "감자튀김", "솔직히 이게 제일 좋아", 2000, "사이드", emptyList()),
        MenuItem("12", "감자튀김 L", "이거 두개면 배 꽤 차는듯", 2600, "사이드", emptyList()),
        MenuItem("13", "치킨너겟", "쿠폰있으면 한번씩 먹기 좋은", 3000, "사이드", emptyList()),
        MenuItem("14", "해쉬 브라운", "냉동으로 사서 돌려먹기 좋은", 1800, "사이드", emptyList()),

        MenuItem("21", "콜라", "전통의 강자", 1500, "음료", emptyList()),
        MenuItem("22", "제로 콜라", "신흥 강자", 1500, "음료", emptyList()),
        MenuItem("23", "사이다", "난 콜라보다 얘가 더 좋더라", 1500, "음료", emptyList()),
        MenuItem("24", "제로 사이다", "좋긴 한데, 이거마실거면 난 제로콜라 마실듯", 1500, "음료", emptyList()),
        MenuItem("25", "아이스티", "복숭아 맛 아이스티", 2000, "음료", emptyList()),

        MenuItem("31", "바닐라 아이스크림", "언제 1500원 됐을까", 1500, "디저트", emptyList()),
        MenuItem("32", "애플 파이", "어머니가 제일 좋아하시는", 2500, "디저트", emptyList()),
        MenuItem("33", "선데이 아이스크림", "컵에 주는거 말고 차이없지 않나", 2500, "디저트", emptyList())
    )
    val categories = listOf("버거", "사이드", "음료", "디저트")

    val sideMenuForSet = menuItems.filter { it.category == "사이드" }
    val drinkMenuForSet = menuItems.filter { it.category == "음료" }

    val recommendationItems: List<MenuItem>
        get() = menuItems
            .filter { it.category == "사이드" || it.category == "디저트" || it.category == "음료" }
            .shuffled()
            .take(4)

    fun init(isPractice: Boolean) {
        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _practiceStep.value = if (isPractice) 0 else -1
        _selectedCategory.value = "버거"
        _paymentStep.value = PaymentStep.NONE
        _selectedPaymentMethod.value = null
        resetSetOrderState()

        if (!isPractice) {
            val missions = listOf(
                Mission("새우버거 3개, 콜라 1잔을 주문해보세요", listOf(RequiredItem("새우버거", 3), RequiredItem("콜라", 1))),
                Mission("불고기버거 2개, 감자튀김 1개를 주문해보세요", listOf(RequiredItem("불고기버거", 2), RequiredItem("감자튀김", 1))),
                Mission("치즈버거 1개, 바닐라 아이스크림 1개를 주문해보세요", listOf(RequiredItem("치즈버거", 1), RequiredItem("바닐라 아이스크림", 1))),
            )
            _currentMission.value = missions.random()
        } else {
            _currentMission.value = null
        }
    }

    fun startSetOrder(burgerItem: MenuItem) {
        _currentSetBurger.value = burgerItem
        _isSelectingSetComponents.value = true
        _selectedCategory.value = "사이드"
        if (_currentMission.value == null) _practiceStep.value = 5
    }

    fun resetSetOrderState() {
        _isSelectingSetComponents.value = false
        _currentSetBurger.value = null
        _selectedCategory.value = "버거"
        if (_currentMission.value == null && _practiceStep.value > 0) _practiceStep.value = 3
    }

    fun completeSetOrder(side: MenuItem, drink: MenuItem) {
        val burger = _currentSetBurger.value ?: return
        val setOption = burger.options.find { it.id == "set" } ?: return

        addToCart(burger, false, setOption)

        _cart.value = _cart.value.toMutableList().apply {
            add(CartItem(side, 1, isSetComponent = true))
        }

        _cart.value = _cart.value.toMutableList().apply {
            add(CartItem(drink, 1, isSetComponent = true))
        }

        updateTotal()
        resetSetOrderState()
    }

    fun startPractice() { _practiceStep.value = 1 }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (_currentMission.value == null && _practiceStep.value == 1) _practiceStep.value = 2
    }

    fun addToCart(item: MenuItem, isPractice: Boolean, option: ItemOption? = null) {
        val currentCart = _cart.value.toMutableList()

        val existing = currentCart.find { it.menuItem.id == item.id && it.option == option && !it.isSetComponent}

        if (existing != null) {
            existing.quantity += 1
        } else {
            currentCart.add(CartItem(item, 1, option = option))
        }
        _cart.value = currentCart
        updateTotal()

        if (!_isSelectingSetComponents.value && _currentMission.value == null && _practiceStep.value == 2) _practiceStep.value = 3
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
            val finalPrice = if (it.isSetComponent) 0 else (basePrice + optionPrice)
            finalPrice * it.quantity
        }
    }

    // 결제 플로우 시작
    fun startPayment() {
        _paymentStep.value = PaymentStep.METHOD_SELECT
    }

    // 결제 방식 선택
    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
        _paymentStep.value = when (method) {
            "CARD" -> PaymentStep.CARD_INSERT
            "QR" -> PaymentStep.QR_SCAN
            else -> PaymentStep.METHOD_SELECT
        }

        // 2초 후 자동으로 결제 처리로 넘어감
        viewModelScope.launch {
            delay(2000)
            processPayment()
        }
    }

    // 결제 처리
    private suspend fun processPayment() {
        _paymentStep.value = PaymentStep.PROCESSING
        delay(2000) // 결제 처리 시뮬레이션
        _paymentStep.value = PaymentStep.COMPLETE
    }

    // 결제 완료 후 주문 완료 처리
    fun checkout(isPractice: Boolean) {
        val mission = _currentMission.value
        if (!isPractice && mission != null) {
            val success = checkMissionSuccess(mission, _cart.value)
            _orderResult.value = if (success) "success" else "fail"
            saveHistory(mission, success)
        } else {
            _orderResult.value = "complete"
        }
        if (isPractice && _practiceStep.value == 3) _practiceStep.value = 4

        // 결제 상태 초기화
        _paymentStep.value = PaymentStep.NONE
        _selectedPaymentMethod.value = null
    }

    // 결제 취소
    fun cancelPayment() {
        _paymentStep.value = PaymentStep.NONE
        _selectedPaymentMethod.value = null
    }

    private fun checkMissionSuccess(mission: Mission, cart: List<CartItem>): Boolean {
        if (cart.size != mission.required.size) return false
        return mission.required.all { req ->
            cart.find { it.menuItem.name == req.name }?.quantity == req.quantity
        }
    }

    private fun saveHistory(mission: Mission, success: Boolean) {
        val dateFormat = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
        val record = DataHistoryRecord(
            id = System.currentTimeMillis().toString(),
            date = dateFormat.format(Date()),
            mission = mission.description,
            success = success,
            userOrder = _cart.value.map { DataRequiredItem(it.menuItem.name, it.quantity) },
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch { repository.saveHistory(record) }
    }
}