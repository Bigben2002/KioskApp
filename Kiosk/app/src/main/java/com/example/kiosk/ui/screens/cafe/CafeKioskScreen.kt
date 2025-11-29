package com.example.kiosk.ui.screens.cafe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiosk.data.model.CartItem
import com.example.kiosk.data.model.MenuItem
import com.example.kiosk.data.model.Mission
import com.example.kiosk.ui.components.KioskCard
import java.text.NumberFormat
import java.util.Locale

// [해석] 연습 모드 가이드 문구를 관리하는 유틸리티 클래스
class CafePracticeStep(val value: Int) {
    val description: String
        get() = when (value) {
            0 -> "화면 하단의 '시작하기' 버튼을 눌러주세요"
            1 -> "원하시는 식사 장소(매장/포장)를 선택해주세요"
            2 -> "메뉴를 터치해서 선택해주세요 (옵션이 있다면 선택해주세요)"
            3 -> "하단 결제 버튼을 눌러 장바구니를 확인하고 결제해주세요"
            else -> ""
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeKioskScreen(
    isPracticeMode: Boolean, // 연습 모드 여부
    onExit: () -> Unit, // 종료 시 실행할 함수 (뒤로가기 등)
    // [해석] Hilt나 Factory 없이 기본 viewModel() 함수로 인스턴스를 가져옵니다.
    viewModel: CafeKioskViewModel = viewModel()
) {
    // === 1. ViewModel 상태 구독 (StateFlow -> State) ===
    // [해석] ViewModel의 데이터가 변하면, 이 변수들도 자동으로 업데이트되어 화면이 다시 그려집니다(Recomposition).
    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()
    val practiceStep by viewModel.practiceStep.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // === 2. 화면 내부 지역 상태 ===
    // 식사 장소 (null이면 선택 전, "매장"/"포장")
    var diningMethod by remember { mutableStateOf<String?>(null) }
    // 장바구니 다이얼로그 표시 여부
    var showCartDialog by remember { mutableStateOf(false) }
    // 옵션 선택을 위해 클릭한 메뉴 (null이 아니면 옵션 팝업 뜸)
    var selectedMenuItemForOption by remember { mutableStateOf<MenuItem?>(null) }

    // 결제 프로세스 단계 관리 ("MENU" -> "PAY_METHOD" -> "PAY_PROCESS" -> "PAY_SUCCESS")
    var paymentStep by remember { mutableStateOf("MENU") }
    var selectedPaymentMethod by remember { mutableStateOf("") }

    val cafeThemeColor = Color(0xFF6F4E37) // 카페 테마색 (갈색)

    // [해석] 화면이 처음 켜질 때 한 번만 실행되는 초기화 코드
    LaunchedEffect(Unit) {
        viewModel.init(isPracticeMode)
    }

    // === 3. 화면 라우팅 (조건부 렌더링) ===

    // (1) 주문 결과 화면 (성공/실패)
    if (orderResult != null) {
        OrderResultScreen(
            result = orderResult!!,
            mission = currentMission,
            cart = cart,
            totalPrice = totalPrice,
            onExit = onExit
        )
        return // 이후 코드는 실행하지 않고 종료
    }

    // (2) 결제 진행 화면들 (결제 수단 선택 -> 처리 -> 성공)
    if (paymentStep != "MENU") {
        if (paymentStep == "PAY_METHOD") {
            CafePaymentMethodSelectScreen(
                onPaid = { method ->
                    selectedPaymentMethod = method
                    paymentStep = "PAY_PROCESS" // 다음 단계로
                },
                onBack = { paymentStep = "MENU" }
            )
        } else if (paymentStep == "PAY_PROCESS") {
            // 가상의 결제 대기 시간 (2초)
            var isProcessing by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                isProcessing = true
                kotlinx.coroutines.delay(2000)
                paymentStep = "PAY_SUCCESS" // 결제 성공
            }
            // 카드 삽입 애니메이션 or QR 스캔 화면 표시
            if (isProcessing) CafePaymentProcessingScreen()
            else if (selectedPaymentMethod == "CARD") CafePaymentCardInsertScreen()
            else CafePaymentQrScanScreen()
        } else if (paymentStep == "PAY_SUCCESS") {
            CafePaymentSuccessScreen(
                cart = cart,
                totalPrice = totalPrice,
                diningMethod = diningMethod ?: "매장",
                isPracticeMode = isPracticeMode,
                onDone = {
                    viewModel.checkout(isPracticeMode) // 최종 데이터 처리(DB저장 등)

                    // [해석] 연습모드는 바로 종료, 실전모드는 결과화면(OrderResult)으로 이동
                    if (isPracticeMode) {
                        onExit()
                    } else {
                        paymentStep = "MENU" // checkout()에 의해 orderResult가 세팅되면 위쪽 (1)번 블록이 실행됨
                    }
                }
            )
        }
        return
    }

    // (3) 메인 화면 구조 (Scaffold: 상단바, 하단바, 내용)
    Scaffold(
        topBar = {
            // 상황에 따라 제목 변경
            val titleText = when {
                isPracticeMode -> "키오스크 연습"
                diningMethod == null -> "카페"
                else -> "카페 ($diningMethod)"
            }

            TopAppBar(
                title = { Text(titleText, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // [해석] 뒤로가기 로직: 장소선택 안했으면 앱종료, 했으면 장소선택 취소
                    IconButton(onClick = {
                        when {
                            diningMethod == null -> onExit()
                            else -> {
                                diningMethod = null
                                viewModel.clearCart() // 장소 바꾸면 장바구니 초기화
                                if (isPracticeMode) {
                                    viewModel.setPracticeStep(1) // 연습 단계 되돌리기
                                }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cafeThemeColor)
            )
        },
        bottomBar = {
            // [해석] 장소가 선택되었고, 장바구니에 물건이 있을 때만 '결제하기' 바 표시
            if (diningMethod != null && cart.isNotEmpty()) {
                BottomAppBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    Button(
                        onClick = { showCartDialog = true }, // 장바구니 열기
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cafeThemeColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        // ... 버튼 내부 내용 (아이콘, 수량, 총 금액) ...
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("장바구니", fontSize = 18.sp)
                                // ... 수량 배지 등 ...
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${cart.sumOf { it.quantity }}",
                                            color = cafeThemeColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {
            // 가이드 메시지 (연습모드 or 미션가이드)
            if (isPracticeMode) CafePracticeGuide(step = practiceStep)
            if (!isPracticeMode && currentMission != null) MissionGuide(mission = currentMission!!.text)

            // ✅ [핵심] 화면 내용 갈아끼우기 로직
            when {
                // A. 연습 시작 전 환영 화면
                isPracticeMode && practiceStep == 0 -> {
                    WelcomeScreen(onStart = { viewModel.startPractice() })
                }
                // B. 매장/포장 선택 화면
                diningMethod == null -> {
                    CafeIntroScreen(onSelect = { selection ->
                        diningMethod = selection
                        if (isPracticeMode && practiceStep == 1) {
                            viewModel.setPracticeStep(2) // 연습 단계 진행
                        }
                    })
                }
                // C. 메인 메뉴판 (카테고리 + 메뉴 리스트)
                else -> {
                    CategoryTabs(
                        categories = viewModel.categories,
                        selectedCategory = selectedCategory,
                        themeColor = cafeThemeColor,
                        onSelect = { category -> viewModel.selectCategory(category) }
                    )
                    MenuList(
                        // 현재 선택된 카테고리의 메뉴만 필터링해서 보여줌
                        menuItems = viewModel.menuItems.filter { it.category == selectedCategory },
                        defaultIcon = "☕️",
                        themeColor = cafeThemeColor,
                        onAdd = { item ->
                            // 옵션이 있는 메뉴면 -> 다이얼로그 띄움
                            if (item.options.isNotEmpty()) {
                                selectedMenuItemForOption = item
                            } else {
                                // 옵션 없으면 -> 바로 장바구니 담기
                                viewModel.addToCart(item, isPracticeMode)
                            }
                        }
                    )
                }
            }
        }
    }
    // === 4. 다이얼로그 (팝업) 처리 ===

    // 옵션 선택 다이얼로그
    if (selectedMenuItemForOption != null) {
        CafeOptionDialog(
            menuItem = selectedMenuItemForOption!!,
            themeColor = cafeThemeColor,
            onDismiss = { selectedMenuItemForOption = null },
            onAddToCart = { selectedOption, quantity -> // 옵션 다이얼로그에서 완료 시
                viewModel.addToCart(selectedMenuItemForOption!!, isPracticeMode, selectedOption, quantity)
                selectedMenuItemForOption = null
            }
        )
    }
    // 장바구니 확인 다이얼로그
    if (showCartDialog) {
        CafeCartDialog(
            cart = cart,
            totalPrice = totalPrice,
            themeColor = cafeThemeColor,
            onDismiss = { showCartDialog = false },
            onUpdateQty = viewModel::updateQuantity, // 함수 참조 전달
            onCheckout = { showCartDialog = false; paymentStep = "PAY_METHOD" } // 결제 단계로 진입
        )
    }
}

// [해석] 카테고리 탭 (커피, 음료, 디저트 등)
@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    themeColor: Color,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            // 선택된 카테고리는 테마색(갈색), 아니면 회색으로 표시
            val isSelected = category == selectedCategory
            Button(
                onClick = { onSelect(category) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) themeColor else Color(0xFFF3F4F6),
                    contentColor = if (isSelected) Color.White else Color(0xFF374151)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(category, fontSize = 18.sp)
            }
        }
    }
}

// [해석] 그리드(격자) 형태의 메뉴 리스트
@Composable
fun MenuList(
    menuItems: List<MenuItem>,
    defaultIcon: String,
    themeColor: Color,
    onAdd: (MenuItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2열로 배치
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(menuItems) { item ->
            KioskCard(
                onClick = { onAdd(item) },
                modifier = Modifier.fillMaxWidth()
            ) {
                // ... 이미지, 가격, +버튼 UI ...
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(defaultIcon, fontSize = 64.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${NumberFormat.getNumberInstance(Locale.KOREA).format(item.price)}원",
                            fontSize = 16.sp,
                            color = Color(0xFF4B5563)
                        )
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = themeColor,
                            modifier = Modifier
                                .background(themeColor.copy(alpha = 0.1f), CircleShape)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// [해석] 매장/포장 선택 화면 (큰 버튼 2개)
@Composable
fun CafeIntroScreen(
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalCafe,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF6F4E37) // 커피색
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "식사 장소를 선택해주세요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(48.dp))
        // ... 타이틀 ...
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectionCard(
                title = "매장 식사",
                icon = Icons.Outlined.Store,
                modifier = Modifier.weight(1f),
                onClick = { onSelect("매장") }
            )
            SelectionCard(
                title = "포장 하기",
                icon = Icons.Outlined.ShoppingBag,
                modifier = Modifier.weight(1f),
                onClick = { onSelect("포장") }
            )
        }
    }
}
@Composable
fun SelectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(250.dp) // 버튼 높이
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)), // 연한 회색 배경
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF6F4E37)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

// [해석] 연습 모드 상단 파란색 가이드 바
@Composable
fun CafePracticeGuide(step: Int) {
    val message = CafePracticeStep(step).description
    if (message.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2563EB)) // 파란색
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun CafeCartDialog(
    cart: List<CartItem>,
    totalPrice: Int,
    themeColor: Color,
    onDismiss: () -> Unit,
    onUpdateQty: (String, Int) -> Unit,
    onCheckout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // ... 상단 타이틀 ...
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("장바구니", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.Gray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                // 장바구니 리스트
                if (cart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("장바구니가 비었습니다", fontSize = 18.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cart) { item ->
                            // [해석] 각 아이템을 그리는 함수 호출
                            CartItemRow(item = item, onUpdateQty = onUpdateQty)
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // ... 총 금액 및 결제하기 버튼 ...
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("총 금액", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }

                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = cart.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor,
                        disabledContainerColor = Color(0xFFFCA5A5)
                    )
                ) {
                    Text("결제하기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// [해석] 장바구니의 한 줄(Row)을 담당하는 컴포넌트
@Composable
private fun CartItemRow(
    item: CartItem,
    onUpdateQty: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 메뉴 정보 표시
        Column(modifier = Modifier.weight(1f)) {
            Text(item.menuItem.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)

            // ✅ [중요] 옵션 표시 로직
            if (item.selectedOptions.isNotEmpty()) {
                val optionString = item.selectedOptions.joinToString(", ") { it.name }
                Text(
                    text = "($optionString)",
                    fontSize = 14.sp,
                    color = Color(0xFF2563EB)
                )
            }
            // (햄버거 키오스크 등 단일 옵션 호환성)
            else if (item.selectedOption != null) {
                Text(
                    text = "(${item.selectedOption.name})",
                    fontSize = 14.sp,
                    color = Color(0xFF2563EB)
                )
            }
            // ✅ 가격 계산: (기본가 + 옵션들의 가격 합) * 수량
            val optionsPrice = item.selectedOptions.sumOf { it.price } + (item.selectedOption?.price ?: 0)
            val totalPrice = (item.menuItem.price + optionsPrice) * item.quantity

            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // 2. 수량 조절 버튼 (-, 숫자, +)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onUpdateQty(item.menuItem.id, -1) },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Icon(Icons.Default.Remove, "감소", modifier = Modifier.size(16.dp), tint = Color.Gray)
            }

            Text(
                text = "${item.quantity}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.widthIn(min = 24.dp),
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                onClick = { onUpdateQty(item.menuItem.id, 1) },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Icon(Icons.Default.Add, "증가", modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    // [해석] 화면 정중앙에 정렬된 컬럼(Column)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // [해석] 크고 직관적인 이모지와 환영 문구
        Text("👋", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("환영합니다!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "주문을 시작하려면\n아래 버튼을 눌러주세요",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(40.dp))

        // [해석] '시작하기' 버튼. 클릭 시 onStart 함수(ViewModel의 초기화 등) 실행
        Button(
            onClick = onStart,
            modifier = Modifier
                .height(64.dp)
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)) // 파란색
        ) {
            Text("시작하기", fontSize = 24.sp)
        }
    }
}

@Composable
fun MissionGuide(mission: String) {
    // [해석] 화면 상단이나 중간에 뜨는 주황색 띠 (미션 내용 표시)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEA580C)) // 진한 주황색 배경
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("🎯 $mission", color = Color.White, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderResultScreen(
    result: String,         // "success", "fail", "complete" 중 하나
    mission: Mission?,      // 실패 시 보여줄 원본 미션 정보
    cart: List<CartItem>,   // 영수증에 보여줄 장바구니 목록
    totalPrice: Int,        // 총 결제 금액
    onExit: () -> Unit      // '처음으로' 버튼 클릭 시 실행할 함수
) {
    // [해석] 1. 결과 상태에 따라 테마 색상 결정 (성공/완료=초록, 실패=빨강)
    val themeColor = when (result) {
        "fail" -> Color(0xFFDC2626) // 빨간색 (경고 느낌)
        else -> Color(0xFF16A34A)   // 초록색 (긍정 느낌)
    }

    // [해석] 2. 결과에 따른 아이콘과 제목 결정
    val resultIcon = if (result == "fail") Icons.Default.Close else Icons.Default.Check
    val resultTitle = when (result) {
        "success" -> "미션 성공!"
        "fail" -> "미션 실패"
        else -> "주문 완료" // 연습 모드일 때
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        resultTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // 오른쪽 상단 홈 아이콘 (비상 탈출구)
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Home, contentDescription = "홈으로", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColor) // 상단바 색상 적용
            )
        }
    ) { padding ->
        // [해석] 스크롤 가능한 메인 컨텐츠 영역
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()) // 내용이 길어지면 스크롤 가능하게 설정
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // [해석] 결과 아이콘 (동그라미 배경 + 아이콘)
            Surface(
                shape = CircleShape,
                color = themeColor,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        resultIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // [해석] 큰 글씨 결과 메시지
            Text(
                text = if (result == "success") "미션 성공! 🎉" else if (result == "fail") "미션 실패" else "주문 완료!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // [해석] 상세 설명 (줄바꿈 포함)
            Text(
                text = when (result) {
                    "success" -> "정확하게 주문하셨습니다!\n정말 잘하셨어요!"
                    "fail" -> "주문이 미션과 다릅니다"
                    else -> "주문이 접수되었습니다\n번호표를 받아 기다려주세요"
                },
                fontSize = 18.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // [해석] ⭐️ 실패했을 경우에만 보이는 '미션 리마인드' 카드
            // "아 맞다, 이거 시키라고 했었지!" 하고 알 수 있게 해줌
            if (result == "fail" && mission != null) {
                KioskCard(
                    backgroundColor = Color(0xFFFEFCE8), // 연한 노란색 배경
                    borderColor = Color(0xFFFEF08A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "미션",
                            fontSize = 16.sp,
                            color = Color(0xFF854D0E),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(mission.text, fontSize = 18.sp, color = Color(0xFF713F12))
                    }
                }
            }

            // [해석] 영수증(주문 내역) 카드
            KioskCard(
                backgroundColor = Color(0xFFF9FAFB), // 회색조 배경
                borderColor = Color(0xFFE5E7EB),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "주문 내역",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 장바구니 아이템 반복 출력
                    cart.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // 1. 메뉴 이름과 수량
                                Text(
                                    "${item.menuItem.name} × ${item.quantity}",
                                    fontSize = 18.sp,
                                    color = Color(0xFF374151),
                                    fontWeight = FontWeight.Medium
                                )

                                // [해석] 2. 옵션 상세 표시 로직 (중요!)
                                // 카페 메뉴(옵션 여러 개)와 버거 메뉴(옵션 1개 or 없음)를 모두 지원하는 코드
                                if (item.selectedOptions.isNotEmpty()) {
                                    // List<Option>을 "HOT, 샷 추가" 같은 문자열로 변환
                                    val optionStr =
                                        item.selectedOptions.joinToString(", ") { it.name }
                                    Text(
                                        text = "└ $optionStr", // 'ㄴ' 모양으로 하위 항목임을 표시
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                } else if (item.selectedOption != null) {
                                    // (구버전 호환) 단일 옵션일 경우
                                    Text(
                                        text = "└ ${item.selectedOption.name}",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 구분선
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // 총 금액 표시
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("총 금액", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor // 결과에 따라 금액 색상도 바뀜 (초록/빨강)
                        )
                    }
                }
            }

            // [해석] 하단 '처음으로' 버튼
            Button(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor) // 버튼도 테마 색상 따라감
            ) {
                Text("처음으로", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}