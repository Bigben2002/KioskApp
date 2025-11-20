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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.example.kiosk.ui.components.KioskCard
import com.example.kiosk.ui.screens.OrderResultScreen
import com.example.kiosk.ui.screens.WelcomeScreen
import com.example.kiosk.ui.screens.MissionGuide
import java.text.NumberFormat
import java.util.Locale

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
    isPracticeMode: Boolean,
    onExit: () -> Unit,
    // 1. 데이터는 카페 전용 ViewModel을 사용합니다.
    viewModel: CafeKioskViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()
    val practiceStep by viewModel.practiceStep.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()


    // 2. ViewModel의 상태를 구독
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var diningMethod by remember { mutableStateOf<String?>(null) }

    var showCartDialog by remember { mutableStateOf(false) }
    var selectedMenuItemForOption by remember { mutableStateOf<MenuItem?>(null) }

    var paymentStep by remember { mutableStateOf("MENU") }
    var selectedPaymentMethod by remember { mutableStateOf("") }

    // 3. 카페 테마 색상 (갈색)
    val cafeThemeColor = Color(0xFF6F4E37)

    // 초기화
    LaunchedEffect(Unit) {
        viewModel.init(isPracticeMode)
    }

    if (orderResult != null) {
        OrderResultScreen(
            result = orderResult!!,
            mission = currentMission,
            cart = cart,
            totalPrice = totalPrice,
            onExit = onExit
        )
        return
    }

    if (paymentStep != "MENU") {
        if (paymentStep == "PAY_METHOD") {
            CafePaymentMethodSelectScreen(
                onPaid = { method ->
                    selectedPaymentMethod = method
                    paymentStep = "PAY_PROCESS"
                },
                onBack = { paymentStep = "MENU" }
            )
        } else if (paymentStep == "PAY_PROCESS") {
            // ... 결제 진행 로직 ...
            var isProcessing by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                isProcessing = true
                kotlinx.coroutines.delay(2000)
                paymentStep = "PAY_SUCCESS"
            }
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
                    viewModel.checkout(isPracticeMode)

                    // ✅ [수정] 모드에 따라 갈림길 만들기
                    if (isPracticeMode) {
                        onExit() // 연습 모드 -> 바로 홈으로! (결과 화면 건너뜀)
                    } else {
                        paymentStep = "MENU" // 실전 모드 -> 결과 화면(OrderResultScreen) 보여줌
                    }
                }
            )
        }
        return
    }
    Scaffold(
        topBar = {
            // 제목 동적 설정
            val titleText = when {
                isPracticeMode -> "키오스크 연습"
                diningMethod == null -> "카페"
                else -> "카페 ($diningMethod)"
            }

            TopAppBar(
                title = { Text(titleText, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // 뒤로가기 로직
                    IconButton(onClick = {
                        when {
                            // 1. 장소 선택 화면이면 -> 종료
                            diningMethod == null -> onExit()
                            // 2. 메뉴판 화면이면 -> 장소 선택으로 돌아감
                            else -> {
                                diningMethod = null

                                viewModel.clearCart()

                                if (isPracticeMode) {
                                    viewModel.setPracticeStep(1)
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
            // 메뉴판 화면(diningMethod가 선택됨)일 때만 장바구니 표시
            if (diningMethod != null && cart.isNotEmpty()) {
                BottomAppBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    Button(
                        onClick = { showCartDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cafeThemeColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("장바구니", fontSize = 18.sp)
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
            // 가이드 메시지 (항상 표시)
            if (isPracticeMode) CafePracticeGuide(step = practiceStep)
            if (!isPracticeMode && currentMission != null) MissionGuide(mission = currentMission!!.text)

            // ✅ [핵심] 화면 내용 갈아끼우기
            when {
                // [A] 연습 모드이고 시작 전 -> 환영 화면
                isPracticeMode && practiceStep == 0 -> {
                    WelcomeScreen(onStart = { viewModel.startPractice() })
                }

                // [B] 장소 선택 전 -> 인트로 화면
                diningMethod == null -> {
                    CafeIntroScreen(onSelect = { selection ->
                        diningMethod = selection
                        if (isPracticeMode && practiceStep == 1) {
                            viewModel.setPracticeStep(2)
                        }
                    })
                }

                // [C] 그 외 -> 메뉴판 화면
                else -> {
                    CategoryTabs(
                        categories = viewModel.categories,
                        selectedCategory = selectedCategory,
                        themeColor = cafeThemeColor,
                        onSelect = { category -> viewModel.selectCategory(category) }
                    )
                    MenuList(
                        menuItems = viewModel.menuItems.filter { it.category == selectedCategory },
                        defaultIcon = "☕",
                        themeColor = cafeThemeColor,
                        onAdd = { item ->
                            if (item.options.isNotEmpty()) {
                                selectedMenuItemForOption = item
                            } else {
                                viewModel.addToCart(item, isPracticeMode)
                            }
                        }
                    )
                }
            }
        }
    }

    // 다이얼로그들
    if (selectedMenuItemForOption != null) {
        CafeOptionDialog(
            menuItem = selectedMenuItemForOption!!,
            themeColor = cafeThemeColor,
            onDismiss = { selectedMenuItemForOption = null },
            onAddToCart = { selectedOption, quantity ->
                viewModel.addToCart(selectedMenuItemForOption!!, isPracticeMode, selectedOption, quantity)
                selectedMenuItemForOption = null
            }
        )
    }

    if (showCartDialog) {
        CafeCartDialog(
            cart = cart,
            totalPrice = totalPrice,
            themeColor = cafeThemeColor,
            onDismiss = { showCartDialog = false },
            onUpdateQty = viewModel::updateQuantity,
            onCheckout = { showCartDialog = false; paymentStep = "PAY_METHOD" }
        )
    }
}
// =======================================================
// 아래는 기존 UI 컴포넌트들을 재사용합니다.
// (이미 KioskSimulatorScreen 파일에 있다면 import해서 쓰면 되고,
//  없다면 아래 코드를 사용하세요.)
// =======================================================

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

@Composable
fun MenuList(
    menuItems: List<MenuItem>,
    defaultIcon: String,
    themeColor: Color,
    onAdd: (MenuItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(menuItems) { item ->
            KioskCard(
                onClick = { onAdd(item) },
                modifier = Modifier.fillMaxWidth()
            ) {
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
        // 상단 로고나 환영 문구
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

        // 선택 버튼 (가로 배치)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 매장 버튼
            SelectionCard(
                title = "매장 식사",
                icon = Icons.Outlined.Store,
                modifier = Modifier.weight(1f),
                onClick = { onSelect("매장") }
            )

            // 포장 버튼
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
@Composable
fun CafePracticeGuide(step: Int) {
    // 우리가 만든 CafePracticeStep 클래스에서 문구를 가져옵니다.
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
                            // 🚨 [핵심] 여기서 파일 맨 아래에 있는 CartItemRow를 호출합니다!
                            CartItemRow(item = item, onUpdateQty = onUpdateQty)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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
        // 1. 메뉴 이름과 옵션 표시
        Column(modifier = Modifier.weight(1f)) {
            Text(item.menuItem.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)

            // ✅ [핵심] 카페용 다중 옵션 리스트 표시
            if (item.selectedOptions.isNotEmpty()) {
                // 예: "HOT, 샷 추가" 처럼 콤마로 연결
                val optionString = item.selectedOptions.joinToString(", ") { it.name }
                Text(
                    text = "($optionString)",
                    fontSize = 14.sp,
                    color = Color(0xFF2563EB) // 파란색 강조
                )
            }
            // (기존 버거 코드 호환용 - 단일 옵션)
            else if (item.selectedOption != null) {
                Text(
                    text = "(${item.selectedOption.name})",
                    fontSize = 14.sp,
                    color = Color(0xFF2563EB)
                )
            }

            // ✅ [핵심] 가격 계산 (리스트에 있는 모든 옵션 가격 합산)
            val optionsPrice = item.selectedOptions.sumOf { it.price } + (item.selectedOption?.price ?: 0)
            val totalPrice = (item.menuItem.price + optionsPrice) * item.quantity

            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // 2. 수량 조절 버튼
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