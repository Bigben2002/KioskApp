package com.example.kiosk.ui.screens.restaurant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantKioskScreen(
    isPractice: Boolean,
    onBack: () -> Unit,
    viewModel: RestaurantKioskViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()
    val practiceStep by viewModel.practiceStep.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showOptionDialog by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }

    val themeColor = Color(0xFF8B4513) // 한성국밥 테마 색상

    LaunchedEffect(Unit) {
        viewModel.init(isPractice)
    }

    // 주문 결과 다이얼로그
    if (orderResult != null) {
        OrderResultDialog(
            result = orderResult!!,
            themeColor = themeColor,
            onDismiss = { onBack() }
        )
    }

    // 옵션 선택 다이얼로그
    if (showOptionDialog && selectedMenuItem != null) {
        RestaurantOptionDialog(
            menuItem = selectedMenuItem!!,
            themeColor = themeColor,
            onDismiss = { showOptionDialog = false },
            onAddToCart = { item, option, porkOption ->
                viewModel.addToCart(item, isPractice, option, porkOption)
                showOptionDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "한성국밥 ${if (isPractice) "연습 모드" else "실전 모드"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor
                )
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAF7F0))
        ) {
            // 왼쪽: 카테고리 영역
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF6B4423))
                    .padding(vertical = 16.dp)
            ) {
                viewModel.categories.forEach { category ->
                    CategoryButton(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { viewModel.selectCategory(category) },
                        themeColor = themeColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 중앙: 메뉴 그리드 + 상단 미션/가이드
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // 미션 또는 연습 가이드
                if (!isPractice && currentMission != null) {
                    MissionCard(mission = currentMission!!)
                    Spacer(modifier = Modifier.height(16.dp))
                } else if (isPractice) {
                    PracticeGuideCard(step = practiceStep, onStart = { viewModel.startPractice() })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 메뉴 그리드
                val filteredMenu = viewModel.menuItems.filter { it.category == selectedCategory }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredMenu) { item ->
                        MenuItemCard(
                            item = item,
                            themeColor = themeColor,
                            onClick = {
                                if (item.category == "국밥류" || item.options.isNotEmpty()) {
                                    selectedMenuItem = item
                                    showOptionDialog = true
                                } else {
                                    viewModel.addToCart(item, isPractice)
                                }
                            },
                            showGuide = isPractice && practiceStep == 2
                        )
                    }
                }
            }

            // 오른쪽: 장바구니
            Column(
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Text(
                    "주문 내역",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 장바구니 아이템
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "메뉴를 선택해주세요",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        cart.forEach { cartItem ->
                            CartItemRow(
                                cartItem = cartItem,
                                onQuantityChange = { delta ->
                                    viewModel.updateQuantity(cartItem.menuItem.id, delta)
                                },
                                themeColor = themeColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Divider(thickness = 2.dp, color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(16.dp))

                // 총액
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("총 금액", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 결제 버튼
                Button(
                    onClick = { viewModel.checkout(isPractice) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = cart.isNotEmpty()
                ) {
                    Text(
                        "결제하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeColor: Color
) {
    val bgColor = if (isSelected) themeColor else Color.Transparent
    val textColor = if (isSelected) Color.White else Color(0xFFD4C4B0)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            category,
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MissionCard(mission: Mission) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E6)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.TaskAlt,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("미션", fontSize = 14.sp, color = Color(0xFFE65100))
                Text(
                    mission.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PracticeGuideCard(step: Int, onStart: () -> Unit) {
    val guideText = when (step) {
        0 -> "연습을 시작하려면 '시작하기' 버튼을 눌러주세요"
        1 -> "1단계: 왼쪽에서 카테고리를 선택해주세요"
        2 -> "2단계: 원하는 메뉴를 선택해주세요"
        3 -> "3단계: 주문 내역을 확인하고 '결제하기'를 눌러주세요"
        else -> "연습을 완료했습니다!"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    guideText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0D47A1)
                )
            }

            if (step == 0) {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("시작하기")
                }
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    themeColor: Color,
    onClick: () -> Unit,
    showGuide: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .then(
                if (showGuide) Modifier.border(
                    3.dp,
                    Brush.horizontalGradient(
                        listOf(themeColor, themeColor.copy(alpha = 0.5f))
                    ),
                    RoundedCornerShape(12.dp)
                )
                else Modifier
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 메뉴 이미지 영역 (이모지로 대체)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (item.category) {
                        "국밥류" -> "🍲"
                        "사이드" -> "🥘"
                        else -> "🥤"
                    },
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 메뉴 이름
            Text(
                item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // 가격
            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(item.price)}원",
                fontSize = 14.sp,
                color = themeColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    themeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cartItem.menuItem.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (cartItem.option != null && cartItem.option.priceDelta > 0) {
                Text(
                    "(${cartItem.option.name})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(
                    (cartItem.menuItem.price + (cartItem.option?.priceDelta ?: 0)) * cartItem.quantity
                )}원",
                fontSize = 14.sp,
                color = themeColor
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onQuantityChange(-1) },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFE5E7EB), CircleShape)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "빼기",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                "${cartItem.quantity}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { onQuantityChange(1) },
                modifier = Modifier
                    .size(32.dp)
                    .background(themeColor, CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "더하기",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun OrderResultDialog(
    result: String,
    themeColor: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("확인")
            }
        },
        title = {
            Text(
                when (result) {
                    "success" -> "미션 성공!"
                    "fail" -> "미션 실패"
                    else -> "주문 완료"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                when (result) {
                    "success" -> "정확하게 주문하셨습니다!"
                    "fail" -> "주문 내역이 미션과 다릅니다. 다시 시도해보세요."
                    else -> "주문이 완료되었습니다."
                }
            )
        },
        icon = {
            Icon(
                when (result) {
                    "success" -> Icons.Default.CheckCircle
                    "fail" -> Icons.Default.Cancel
                    else -> Icons.Default.Check
                },
                contentDescription = null,
                tint = when (result) {
                    "success" -> Color(0xFF4CAF50)
                    "fail" -> Color(0xFFF44336)
                    else -> themeColor
                },
                modifier = Modifier.size(48.dp)
            )
        }
    )
}