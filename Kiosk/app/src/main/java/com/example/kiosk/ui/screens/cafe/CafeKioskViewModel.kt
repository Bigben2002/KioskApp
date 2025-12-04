package com.example.kiosk.ui.screens.cafe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiosk.R
import com.example.kiosk.data.model.*
import com.example.kiosk.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// [해석] AndroidViewModel을 상속받아 'Application' context를 사용할 수 있습니다. (DB 접근 등에 필요)
class CafeKioskViewModel(application: Application) : AndroidViewModel(application) {

    // [해석] 주문 기록을 저장하기 위한 저장소(Repository) 연결
    private val repository = HistoryRepository(application)

    // === 상태 변수들 (StateFlow) ===
    // [해석] UI 상태를 관찰 가능(Observable)하게 관리합니다.
    // 외부에서는 읽기만 가능(asStateFlow)하고, 내부에서만 값 변경(_변수)이 가능하도록 캡슐화했습니다.

    // 1. 장바구니 목록
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    // 2. 총 결제 금액
    private val _totalPrice = MutableStateFlow(0)
    val totalPrice = _totalPrice.asStateFlow()

    // 3. 현재 수행해야 할 미션 (null이면 연습모드거나 미션 없음)
    private val _currentMission = MutableStateFlow<Mission?>(null)
    val currentMission = _currentMission.asStateFlow()

    // 4. 연습 모드 진행 단계 (0: 시작 전, 1~4: 단계별 진행)
    private val _practiceStep = MutableStateFlow(0)
    val practiceStep = _practiceStep.asStateFlow()

    // 5. 주문 결과 상태 ("success", "fail", "complete" 등)
    private val _orderResult = MutableStateFlow<String?>(null)
    val orderResult = _orderResult.asStateFlow()

    // 6. 현재 선택된 메뉴 카테고리 (기본값: 커피)
    private val _selectedCategory = MutableStateFlow("커피")
    val selectedCategory = _selectedCategory.asStateFlow()

    // [해석] 연습 모드 강제 단계 설정 (디버깅이나 특정 상황 이동용)
    fun setPracticeStep(step: Int) {
        _practiceStep.value = step
    }

    // === 1. 메뉴 및 옵션 데이터 정의 ===
    // [해석] 앱 내에서 사용할 정적(Static) 데이터들을 정의하는 곳입니다.
    // 실제 상용 앱이라면 서버나 DB에서 가져오겠지만, 여기서는 하드코딩으로 정의되어 있습니다.

    // 공통 옵션: 얼음 조절
    private val iceAdjustmentOptions = listOf(
        ItemOption("얼음 추가"),
        ItemOption("얼음 적게"),
        ItemOption("얼음 빼기")
    )

    // 커피 옵션: 온도/샷 + 얼음 조절
    private val coffeeOptions = listOf(
        ItemOption("HOT"),
        ItemOption("ICE", 500), // 아이스는 500원 추가
        ItemOption("샷 추가", 500)
    ) + iceAdjustmentOptions // [해석] 리스트 합치기 (+) 연산자 사용

    // 에이드 옵션: 무조건 아이스 + 얼음 조절
    private val adeOptions = listOf(
        ItemOption("ICE Only")
    ) + iceAdjustmentOptions

    // 초코라떼 옵션
    private val chocoOptions = listOf(
        ItemOption("HOT"),
        ItemOption("ICE", 500)
    ) + iceAdjustmentOptions

    // 디저트 옵션 (가격 0원인 단순 선택지)
    val dessertOptions = listOf(
        ItemOption("기본", 0),
        ItemOption("포크 2개", 0)
    )

    // 전체 메뉴 리스트 정의
    val menuItems = listOf(
        // [커피 카테고리]
        MenuItem("c1", "아메리카노", 2000, "커피", coffeeOptions, R.drawable.americano),
        MenuItem("c2", "카페라떼", 3000, "커피", coffeeOptions, R.drawable.cafelatte),
        MenuItem("c3", "바닐라라떼", 3500, "커피", coffeeOptions, R.drawable.vanillalatte),
        MenuItem("c4", "카페모카", 3800, "커피", coffeeOptions, R.drawable.cafemocha),

        // [음료 카테고리]
        MenuItem("d1", "레몬에이드", 3500, "음료", adeOptions, R.drawable.lemonade),
        MenuItem("d2", "아이스티", 3500, "음료", adeOptions, R.drawable.icetea),
        MenuItem("d3", "초코라떼", 4500, "음료", chocoOptions, R.drawable.chocolatelatte),

        // [디저트 카테고리]
        MenuItem("k1", "초코무스 케이크",  5500, "디저트", dessertOptions, R.drawable.chocolatesmoothcake),
        MenuItem("k2", "치즈 케이크", 5500, "디저트", dessertOptions, R.drawable.cheesecake),
        MenuItem("k3", "크로플", 3500, "디저트", dessertOptions, R.drawable.croffle)
    )

    val categories = listOf("커피", "음료", "디저트")

    // === 2. 초기화 및 미션 설정 ===
    // [해석] 화면 진입 시 호출. 연습 모드인지 미션 모드인지에 따라 상태를 초기화합니다.
    fun init(isPractice: Boolean) {
        _cart.value = emptyList()
        _totalPrice.value = 0
        _orderResult.value = null
        _practiceStep.value = if (isPractice) 0 else -1 // 연습모드면 0단계, 아니면 -1(비활성)
        _selectedCategory.value = "커피"

        if (!isPractice) {
            // [해석] 미션 모드일 경우: 미리 정의된 미션 목록 중 하나를 랜덤으로 선택
            val missions = listOf(
                Mission(
                    "따뜻한 아메리카노 3잔을 주문해보세요",
                    listOf(
                        RequiredItem("아메리카노", 3, "HOT")
                    )
                ),
                Mission(
                    "아이스 바닐라라떼(얼음 적게) 1잔을 주문해보세요",
                    listOf(
                        RequiredItem("바닐라라떼", 1, "ICE, 얼음 적게")
                    )
                ),
                Mission(
                    "크로플(포크 2개) 1개를 주문해보세요",
                    listOf(
                        RequiredItem("크로플", 1, "기본, 포크 2개")
                    )
                ),
                Mission(
                    "레몬에이드(얼음 추가) 1잔, 아이스티(얼음 빼기) 1잔을 주문해보세요",
                    listOf(
                        RequiredItem("레몬에이드", 1, "ICE Only, 얼음 추가"),
                        RequiredItem("아이스티", 1, "ICE Only, 얼음 빼기")
                    )
                ),
                Mission(
                    "아이스 초코라떼 1잔과 치즈 케이크 1개를 주문해보세요",
                    listOf(
                        RequiredItem("초코라떼", 1, "ICE, 샷 추가"),
                        RequiredItem("치즈 케이크", 1, "기본")
                    )
                ),
                Mission(
                    "따뜻한 카페모카 1잔, 아이스 아메리카노(얼음 추가) 1잔을 주문해보세요",
                    listOf(
                        RequiredItem("카페모카", 1, "HOT"),
                        RequiredItem("아메리카노", 1, "ICE, 얼음 추가")
                    )
                ),
                Mission(
                    "아이스 아메리카노(샷 추가, 얼음 적게) 1잔, 따뜻한 아메리카노(샷추가) 1잔을 주문해보세요",
                    listOf(RequiredItem("아메리카노", 1, "ICE, 샷 추가, 얼음 적게"),
                        RequiredItem("아메리카노", 1, "HOT, 샷 추가"))
                ),
                Mission(
                    "따뜻한 카페라떼 1잔, 치즈 케이크 1개를 주문해보세요",
                    listOf(RequiredItem("카페라떼", 1, "HOT"), RequiredItem("치즈 케이크", 1, "기본"))
                ),
                Mission(
                    "레몬에이드 1잔, 초코무스 케이크 1개를 주문해보세요",
                    listOf(RequiredItem("레몬에이드", 1, "ICE Only"), RequiredItem("초코무스 케이크", 1, "기본"))
                )
            )
            _currentMission.value = missions.random()
        } else {
            _currentMission.value = null
        }
    }

    // === 3. 연습 모드 단계 관리 ===
    // [해석] 연습 모드에서 가이드를 보여주기 위한 단계(Step) 제어 함수들입니다.
    fun startPractice() { _practiceStep.value = 1 }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        // 연습 1단계(카테고리 선택)였다면 -> 2단계로 진행
        if (_currentMission.value == null && _practiceStep.value == 1) _practiceStep.value = 2
    }

    // === 4. 장바구니 로직 (핵심 기능) ===
    fun addToCart(item: MenuItem, isPractice: Boolean, options: List<ItemOption> = emptyList(), quantity: Int = 1) {
        val currentCart = _cart.value.toMutableList()

        // [해석] 중복 체크: 이미 장바구니에 '같은 메뉴' + '같은 옵션'이 있는지 확인
        val existing = currentCart.find { it.menuItem.id == item.id && it.selectedOptions == options }

        if (existing != null) {
            // 있다면 수량만 증가
            existing.quantity += quantity
        } else {
            // 없다면 새로 추가 (카페용이므로 selectedOptions 사용)
            currentCart.add(
                CartItem(
                    menuItem = item,
                    quantity = quantity,
                    selectedOption = null,
                    selectedOptions = options
                )
            )
        }
        _cart.value = currentCart
        updateTotal() // 금액 재계산

        // 연습 2단계(메뉴 담기)였다면 -> 3단계로 진행
        if (_currentMission.value == null && _practiceStep.value == 2) _practiceStep.value = 3
    }

    // [해석] 장바구니 아이템 수량 변경 (+, - 버튼)
    fun updateQuantity(itemId: String, delta: Int) {
        _cart.value = _cart.value.mapNotNull {
            if (it.menuItem.id == itemId) {
                val newQty = it.quantity + delta
                // 수량이 0 이하면 리스트에서 제거(null 반환), 아니면 수량 업데이트
                if (newQty > 0) it.copy(quantity = newQty) else null
            } else it
        }
        updateTotal()
    }

    // [해석] 총 금액 계산 로직
    private fun updateTotal() {
        _totalPrice.value = _cart.value.sumOf { item ->
            val basePrice = item.menuItem.price
            // 옵션 추가 금액 합산
            val optionsPrice = item.selectedOptions.sumOf { it.price }
            (basePrice + optionsPrice) * item.quantity
        }
    }

    // === 5. 결제 및 미션 검증 ===
    fun checkout(isPractice: Boolean) {
        val mission = _currentMission.value

        // 실전 모드(미션 있음)일 때 검증 수행
        if (!isPractice && mission != null) {
            val success = checkMissionSuccess(mission, _cart.value)
            _orderResult.value = if (success) "success" else "fail"
            saveHistory(mission, success) // 결과 DB 저장
        } else {
            // 연습 모드는 무조건 성공 처리
            _orderResult.value = "complete"
        }

        // 연습 3단계(결제)였다면 -> 4단계(종료)로 이동
        if (isPractice && _practiceStep.value == 3) _practiceStep.value = 4
    }

    // ⚠️ 중요: 카페 미션 검증 로직
    private fun checkMissionSuccess(mission: Mission, cart: List<CartItem>): Boolean {
        // 1. 전체 개수 우선 비교 (불필요한 메뉴를 더 샀으면 실패 처리)
        val cartTotal = cart.sumOf { it.quantity }
        val missionTotal = mission.required.sumOf { it.quantity }
        if (cartTotal != missionTotal) {
            println("❌ 전체 개수 불일치")
            return false
        }

        // 2. 미션의 요구사항(RequiredItem) 하나하나가 장바구니에 있는지 확인
        return mission.required.all { req ->
            // 미션의 요구 옵션 문자열을 Set으로 변환 (순서 상관없이 비교하기 위해)
            // 예: "ICE, 샷 추가" -> {"ICE", "샷 추가"}
            val reqOptionSet = req.option?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

            println("🔎 미션 찾는 중: ${req.name} / 옵션: $reqOptionSet")

            // 장바구니 아이템 중 이름과 옵션 구성이 정확히 일치하는 것들의 수량 합산
            val matchingQuantity = cart.filter { item ->
                val cartOptionSet = item.selectedOptions.map { it.name }.toSet()

                // 이름 일치 && 옵션 집합(Set) 일치 여부 확인
                val isMatch = (item.menuItem.name == req.name) && (reqOptionSet == cartOptionSet)

                if (item.menuItem.name == req.name) {
                    println("   - 장바구니 후보: ${item.menuItem.name} / 옵션: $cartOptionSet -> 일치여부: $isMatch")
                }
                isMatch
            }.sumOf { it.quantity }

            println("   👉 최종 집계 수량: $matchingQuantity / 필요 수량: ${req.quantity}")

            // 요구 수량과 정확히 일치해야 통과
            matchingQuantity == req.quantity
        }
    }

    // [해석] DB에 결과 저장 (비동기 처리)
    private fun saveHistory(mission: Mission, success: Boolean) {
        val dateFormat = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())

        // 장바구니(CartItem) 객체들을 기록용 객체(RequiredItem)로 변환
        val recordedOrder = _cart.value.map { cartItem ->
            val optionString = cartItem.selectedOptions
                .map { it.name }
                .joinToString(", ") // 옵션들을 콤마로 연결 문자열로 변환

            RequiredItem(
                name = cartItem.menuItem.name,
                quantity = cartItem.quantity,
                option = if (optionString.isNotEmpty()) optionString else null
            )
        }

        // DB 레코드 생성
        val record = HistoryRecord(
            id = System.currentTimeMillis().toString(), // 고유 ID (타임스탬프)
            date = dateFormat.format(Date()),
            mission = mission.text,
            success = success,
            userOrder = recordedOrder,
            timestamp = System.currentTimeMillis()
        )
        // 코루틴을 사용하여 백그라운드 스레드에서 저장
        viewModelScope.launch { repository.saveHistory(record) }
    }

    // Getter 함수들
    fun getCurrentCategories() = categories
    fun getCurrentMenuItems() = menuItems

    // 장바구니 비우기
    fun clearCart() {
        _cart.value = emptyList()
        _totalPrice.value = 0
    }
}