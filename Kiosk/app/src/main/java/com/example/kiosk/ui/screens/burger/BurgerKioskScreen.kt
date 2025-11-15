package com.example.kiosk.ui.screens.burger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items

// 재사용 가능한 카드 컴포넌트
@Composable
fun KioskCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.run { if (onClick != null) clickable(onClick = onClick) else this },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        if (borderColor != null) {
            Box(modifier = Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))) {
                content()
            }
        } else {
            content()
        }
    }
}

// 연습 단계 헬퍼 클래스
class PracticeStep(val value: Int) {
    val description: String
        get() = when (value) {
            0 -> "화면 하단의 '시작하기' 버튼을 눌러주세요"
            1 -> "원하시는 메뉴 종류를 선택해주세요"
            2 -> "메뉴를 터치해서 선택해주세요"
            3 -> "하단 결제 버튼을 눌러 장바구니를 확인하고 결제해주세요"
            4 -> "결제 완료 후, 다시 시작하려면 홈 버튼을 눌러주세요"
            5 -> "세트 구성을 위해 사이드 메뉴를 선택해주세요"
            6 -> "세트 구성을 위해 음료를 선택해주세요"
            else -> ""
        }
}

// =================================================================
// BurgerKioskScreen (메인 화면)
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurgerKioskScreen(
    isPracticeMode: Boolean,
    onExit: () -> Unit,
    viewModel: BurgerKioskViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()
    val practiceStep by viewModel.practiceStep.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val isSelectingSetComponents by viewModel.isSelectingSetComponents.collectAsState()
    val currentSetBurger by viewModel.currentSetBurger.collectAsState()
    var showOptionDialog by remember { mutableStateOf<MenuItem?>(null) }

    var selectedSide by remember { mutableStateOf<MenuItem?>(null) }
    var selectedDrink by remember { mutableStateOf<MenuItem?>(null) }
    var showCartDialog by remember { mutableStateOf(false) }
    var showRecommendationDialog by remember { mutableStateOf(false) }
    var itemToAddFromRecommendation by remember { mutableStateOf<MenuItem?>(null) }

    LaunchedEffect(Unit) { viewModel.init(isPracticeMode) }

    LaunchedEffect(selectedSide) {
        if (isSelectingSetComponents && selectedSide != null && selectedCategory == "사이드") {
            viewModel.selectCategory("음료")
            if (isPracticeMode) viewModel.setPracticeStep(6)
        }
    }

    val resetSetSelection = {
        selectedSide = null
        selectedDrink = null
        viewModel.resetSetOrderState()
    }

    LaunchedEffect(itemToAddFromRecommendation) {
        itemToAddFromRecommendation?.let { item ->
            viewModel.addToCart(item, isPracticeMode)
            itemToAddFromRecommendation = null
            showCartDialog = true
        }
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

    val topBarTitle = if (isPracticeMode) {
        "키오스크 연습"
    } else if (currentMission != null) {
        "미션 모드"
    } else {
        "햄버거 가게"
    }

    val guideText = if (isPracticeMode) PracticeStep(practiceStep).description else currentMission?.description ?: ""
    val guideColor = if (isPracticeMode) Color(0xFF2563EB) else Color(0xFFEA580C)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(topBarTitle, color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFDC2626))
                )
                if (practiceStep != 0 || !isPracticeMode) {
                    Box(
                        modifier = Modifier.fillMaxWidth().background(guideColor).padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (!isPracticeMode && currentMission != null) "🎯 $guideText" else guideText,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (practiceStep != 0 || !isPracticeMode) {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("총 금액", fontSize = 16.sp, color = Color.Gray)
                            Text(
                                "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }

                        Button(
                            onClick = { showCartDialog = true },
                            modifier = Modifier.height(60.dp).width(200.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = cart.isNotEmpty()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("결제하기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${cart.sumOf { it.quantity }}",
                                            color = Color(0xFFDC2626),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF9FAFB))) {
            if (isPracticeMode && practiceStep == 0) {
                WelcomeScreen(onStart = { viewModel.startPractice() })
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    CategorySidebar(
                        categories = viewModel.categories,
                        selectedCategory = selectedCategory,
                        onSelect = { category -> viewModel.selectCategory(category) }
                    )
                    MenuListGrid(
                        menuItems = viewModel.menuItems.filter { it.category == selectedCategory },
                        onAdd = { menuItem ->
                            if (isSelectingSetComponents) {
                                if (menuItem.category == "사이드") {
                                    selectedSide = menuItem
                                } else if (menuItem.category == "음료") {
                                    selectedDrink = menuItem
                                }
                            } else if (menuItem.options.isNullOrEmpty() || menuItem.category != "버거") {
                                viewModel.addToCart(menuItem, isPracticeMode)
                            } else {
                                showOptionDialog = menuItem
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCartDialog) {
        CartDialog(
            cart = cart,
            totalPrice = totalPrice,
            onDismiss = { showCartDialog = false },
            onUpdateQty = viewModel::updateQuantity,
            onCheckout = {
                showCartDialog = false
                showRecommendationDialog = true
            }
        )
    }

    showOptionDialog?.let { menuItem ->
        BurgerOptionDialog(
            menuItem = menuItem,
            themeColor = Color(0xFFDC2626),
            onDismiss = { showOptionDialog = null },
            onAddToCart = { item, option ->
                if (option.id == "set") {
                    viewModel.startSetOrder(item)
                } else {
                    viewModel.addToCart(item, isPracticeMode, option)
                }
                showOptionDialog = null
            }
        )
    }

    if (isSelectingSetComponents) {
        SetComponentDialog(
            burger = currentSetBurger,
            sideItems = viewModel.sideMenuForSet,
            drinkItems = viewModel.drinkMenuForSet,
            selectedSide = selectedSide,
            selectedDrink = selectedDrink,
            onSideSelect = { selectedSide = it },
            onDrinkSelect = { selectedDrink = it },
            onDismiss = resetSetSelection,
            onComplete = { side, drink ->
                viewModel.completeSetOrder(side, drink)
                selectedSide = null
                selectedDrink = null
            }
        )
    }

    if (showRecommendationDialog) {
        RecommendationDialog(
            recommendedItems = viewModel.recommendationItems,
            onDismiss = { showRecommendationDialog = false; viewModel.checkout(isPracticeMode) },
            onAddItem = { item -> itemToAddFromRecommendation = item; showRecommendationDialog = false },
            onCancel = { showRecommendationDialog = false; showCartDialog = true }
        )
    }
}

// =================================================================
// 컴포넌트들
// =================================================================

@Composable
fun CategorySidebar(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .fillMaxHeight()
            .background(Color(0xFFF3F4F6))
            .border(width = 1.dp, color = Color(0xFFE5E7EB))
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        categories.forEach { category ->
            CategorySidebarItem(
                category = category,
                isSelected = category == selectedCategory,
                onSelect = onSelect
            )
        }
    }
}

@Composable
fun CategorySidebarItem(
    category: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit
) {
    val themeColor = Color(0xFFDC2626)
    val backgroundColor = if (isSelected) Color(0xFFFFFFFF) else Color.Transparent
    val textColor = if (isSelected) themeColor else Color(0xFF4B5563)
    val iconColor = if (isSelected) themeColor else Color(0xFF6B7280)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(category) }
            .background(backgroundColor, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val icon = when (category) {
            "버거" -> Icons.Outlined.LocalDining
            "사이드" -> Icons.Outlined.Fastfood
            "음료" -> Icons.Outlined.LocalDrink
            "디저트" -> Icons.Outlined.Icecream
            else -> Icons.Outlined.Category
        }
        Icon(imageVector = icon, contentDescription = category, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(category, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun MenuListGrid(menuItems: List<MenuItem>, onAdd: (MenuItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        items(menuItems) { item ->
            MenuItemCard(menuItem = item, onClick = { onAdd(item) })
        }
    }
}

@Composable
fun MenuItemCard(menuItem: MenuItem, onClick: () -> Unit) {
    KioskCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFE5E7EB))
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (menuItem.category) {
                    "버거" -> "🍔"
                    "사이드" -> "🍟"
                    "음료" -> "🥤"
                    "디저트" -> "🍦"
                    else -> "🍽️"
                }
                Text(text = emoji, fontSize = 60.sp)
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = menuItem.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(menuItem.price)}원",
                    fontSize = 14.sp,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
        Button(
            onClick = onStart,
            modifier = Modifier.height(64.dp).width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("시작하기", fontSize = 24.sp)
        }
    }
}

@Composable
fun CartDialog(
    cart: List<CartItem>,
    totalPrice: Int,
    onDismiss: () -> Unit,
    onUpdateQty: (String, Int) -> Unit,
    onCheckout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
                            CartItemRow(item = item, onUpdateQty = onUpdateQty)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("총 금액", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }

                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = cart.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
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
private fun CartItemRow(item: CartItem, onUpdateQty: (String, Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val optionText = if (item.option != null) " (${item.option.name})" else if (item.isSetComponent) " (세트 구성)" else ""
            Text(item.menuItem.name + optionText, fontSize = 18.sp, fontWeight = FontWeight.Medium)

            val priceToDisplay = if (item.isSetComponent) 0 else item.menuItem.price * item.quantity + (item.option?.priceDelta ?: 0)
            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(priceToDisplay)}원",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

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
                Icon(Icons.Default.Remove, contentDescription = "감소", modifier = Modifier.size(16.dp), tint = Color.Gray)
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
                Icon(Icons.Default.Add, contentDescription = "증가", modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderResultScreen(
    result: String,
    mission: Mission?,
    cart: List<CartItem>,
    totalPrice: Int,
    onExit: () -> Unit
) {
    val themeColor = when (result) {
        "fail" -> Color(0xFFDC2626)
        else -> Color(0xFF16A34A)
    }

    val resultIcon = if (result == "fail") Icons.Default.Close else Icons.Default.Check
    val resultTitle = when (result) {
        "success" -> "미션 성공!"
        "fail" -> "미션 실패"
        else -> "주문 완료"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(resultTitle, color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Home, contentDescription = "홈으로", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Surface(shape = CircleShape, color = themeColor, modifier = Modifier.size(100.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(resultIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (result == "success") "미션 성공! 🎉" else if (result == "fail") "미션 실패" else "주문 완료!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
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

            if (result == "fail" && mission != null) {
                KioskCard(
                    backgroundColor = Color(0xFFFEFCE8),
                    borderColor = Color(0xFFFEF08A),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "미션",
                            fontSize = 16.sp,
                            color = Color(0xFF854D0E),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(mission.description, fontSize = 18.sp, color = Color(0xFF713F12))
                    }
                }
            }

            KioskCard(
                backgroundColor = Color(0xFFF9FAFB),
                borderColor = Color(0xFFE5E7EB),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("주문 내역", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                    cart.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val optionText = if (item.option != null) " (${item.option.name})" else if (item.isSetComponent) " (세트 구성)" else ""
                                Text(
                                    text = item.menuItem.name + optionText,
                                    fontSize = 18.sp,
                                    color = Color(0xFF374151),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            val priceToDisplay = if (item.isSetComponent) 0 else item.menuItem.price * item.quantity + (item.option?.priceDelta ?: 0)
                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(priceToDisplay)}원",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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
                            color = themeColor
                        )
                    }
                }
            }

            Button(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("처음으로", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SetComponentDialog(
    burger: MenuItem?,
    sideItems: List<MenuItem>,
    drinkItems: List<MenuItem>,
    selectedSide: MenuItem?,
    selectedDrink: MenuItem?,
    onSideSelect: (MenuItem) -> Unit,
    onDrinkSelect: (MenuItem) -> Unit,
    onDismiss: () -> Unit,
    onComplete: (side: MenuItem, drink: MenuItem) -> Unit
) {
    val themeColor = Color(0xFFDC2626)
    val currentStep = if (selectedSide == null) "사이드 메뉴 선택" else "음료 선택"
    val isReadyToComplete = selectedSide != null && selectedDrink != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 650.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("${burger?.name} 세트 구성", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(currentStep, fontSize = 18.sp, color = themeColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    TabItem(
                        title = "사이드",
                        isSelected = selectedSide != null,
                        themeColor = themeColor,
                        selectedItemName = selectedSide?.name
                    )
                    TabItem(
                        title = "음료",
                        isSelected = selectedSide != null,
                        themeColor = themeColor,
                        selectedItemName = selectedDrink?.name
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedSide == null) {
                        items(sideItems) { item ->
                            SetComponentCard(
                                item = item,
                                isSelected = item == selectedSide,
                                themeColor = themeColor,
                                onClick = onSideSelect
                            )
                        }
                    } else {
                        items(drinkItems) { item ->
                            SetComponentCard(
                                item = item,
                                isSelected = item == selectedDrink,
                                themeColor = themeColor,
                                onClick = onDrinkSelect
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E7EB),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = { onComplete(selectedSide!!, selectedDrink!!) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = isReadyToComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("주문 완료", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    themeColor: Color,
    selectedItemName: String?
) {
    val borderColor = if (isSelected) themeColor else Color(0xFFE5E7EB)

    Column(
        modifier = Modifier
            .height(72.dp)
            .background(if (isSelected) themeColor else Color(0xFFF3F4F6))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = if (isSelected) Color.White else Color(0xFF4B5563),
            fontWeight = FontWeight.Bold
        )
        if (selectedItemName != null) {
            Text(
                selectedItemName,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280),
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SetComponentCard(
    item: MenuItem,
    isSelected: Boolean,
    themeColor: Color,
    onClick: (MenuItem) -> Unit
) {
    val borderColor = if (isSelected) themeColor else Color(0xFFE5E7EB)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColor.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (item.category) {
                    "사이드" -> "🍟"
                    "음료" -> "🥤"
                    else -> "❓"
                },
                fontSize = 40.sp,
                modifier = Modifier.height(40.dp)
            )
            Text(
                item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("추가 금액 없음", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun RecommendationDialog(
    recommendedItems: List<MenuItem>,
    onDismiss: () -> Unit,
    onAddItem: (MenuItem) -> Unit,
    onCancel: () -> Unit
) {
    val themeColor = Color(0xFFDC2626)

    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("이런 메뉴는 어떠세요?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("결제 전 사이드 메뉴를 추가해 보세요!", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(recommendedItems) { item ->
                        RecommendationCard(item = item, themeColor = themeColor, onClick = onAddItem)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E7EB),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("넘어가기")
                    }
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("추가하기", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(item: MenuItem, themeColor: Color, onClick: (MenuItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(item) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (item.category) {
                    "사이드" -> "🍟"
                    "음료" -> "🥤"
                    "디저트" -> "🍦"
                    else -> "❓"
                },
                fontSize = 30.sp,
                modifier = Modifier.height(30.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(item.price)}원",
                fontSize = 12.sp,
                color = themeColor
            )
        }
    }
}