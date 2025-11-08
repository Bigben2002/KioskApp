package com.example.kiosk.ui.screens.simulator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiosk.data.model.MenuItem
import com.example.kiosk.ui.components.KioskButton
import com.example.kiosk.ui.components.KioskCard
import java.text.NumberFormat
import java.util.Locale
import com.example.kiosk.data.model.CartItem
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import com.example.kiosk.data.model.Mission
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskSimulatorScreen(
    isPracticeMode: Boolean,
    onExit: () -> Unit,
    viewModel: KioskViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()
    val practiceStep by viewModel.practiceStep.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()

    var selectedCategory by remember { mutableStateOf("버거") }
    var showCartDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.init(isPracticeMode) }

    if (orderResult != null) {
        OrderResultScreen(result = orderResult!!, mission = currentMission, cart = cart, totalPrice = totalPrice, onExit = onExit)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("햄버거 가게", color = Color.White) },
                navigationIcon = { IconButton(onClick = onExit) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFDC2626))
            )
        },
        bottomBar = {
            if (practiceStep != 0 && cart.isNotEmpty()) {
                BottomAppBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    Button(
                        onClick = { showCartDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("장바구니", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(24.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text("${cart.sumOf { it.quantity }}", color = Color(0xFFDC2626), fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                            Text("${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF9FAFB))) {
            if (isPracticeMode) PracticeGuide(step = practiceStep)
            if (!isPracticeMode && currentMission != null) MissionGuide(mission = currentMission!!.text)

            if (isPracticeMode && practiceStep == 0) {
                WelcomeScreen(onStart = { viewModel.startPractice() })
            } else {
                CategoryTabs(categories = viewModel.categories, selectedCategory = selectedCategory, onSelect = { selectedCategory = it; viewModel.selectCategory(isPracticeMode) })
                MenuList(menuItems = viewModel.menuItems.filter { it.category == selectedCategory }, onAdd = { viewModel.addToCart(it, isPracticeMode) })
            }
        }
    }

    if (showCartDialog) {
        CartDialog(
            cart = cart,
            totalPrice = totalPrice,
            onDismiss = { showCartDialog = false },
            onUpdateQty = viewModel::updateQuantity,
            onCheckout = { showCartDialog = false; viewModel.checkout(isPracticeMode) }
        )
    }
}

@Composable
fun PracticeGuide(step: Int) {
    val messages = listOf("화면 하단의 '시작하기' 버튼을 눌러주세요", "원하시는 메뉴 종류를 선택해주세요", "메뉴를 터치해서 선택해주세요", "하단 장바구니를 눌러 결제해주세요")
    val message = messages.getOrElse(step.coerceAtLeast(1) - 1) { "" }
    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2563EB)).padding(12.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun MissionGuide(mission: String) {
    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFEA580C)).padding(12.dp), contentAlignment = Alignment.Center) {
        Text("🎯 $mission", color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("👋", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("환영합니다!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("주문을 시작하려면\n아래 버튼을 눌러주세요", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onStart, modifier = Modifier.height(64.dp).width(200.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
            Text("시작하기", fontSize = 24.sp)
        }
    }
}

@Composable
fun CategoryTabs(categories: List<String>, selectedCategory: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Button(
                onClick = { onSelect(category) },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFFDC2626) else Color(0xFFF3F4F6), contentColor = if (isSelected) Color.White else Color(0xFF374151)),
                shape = RoundedCornerShape(8.dp)
            ) { Text(category, fontSize = 18.sp) }
        }
    }
}

@Composable
fun MenuList(menuItems: List<MenuItem>, onAdd: (MenuItem) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Fixed(1), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(menuItems) { item ->
            KioskCard(onClick = { onAdd(item) }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("🍔", fontSize = 40.sp) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Text("${NumberFormat.getNumberInstance(Locale.KOREA).format(item.price)}원", fontSize = 18.sp, color = Color(0xFF4B5563))
                    }
                    Icon(Icons.Default.Add, null, tint = Color(0xFFDC2626))
                }
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 헤더: 제목과 닫기 버튼
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

                // 장바구니 아이템 리스트
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
                            CartItemRow(item = item, onUpdateQty = onUpdateQty)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 하단: 총 금액 및 결제 버튼
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
                        color = Color(0xFFDC2626) // red-600
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
                        containerColor = Color(0xFFDC2626), // red-600
                        disabledContainerColor = Color(0xFFFCA5A5) // red-300
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
        // 메뉴 이름과 가격
        Column(modifier = Modifier.weight(1f)) {
            Text(item.menuItem.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(item.menuItem.price * item.quantity)}원",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // 수량 조절 버튼
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onUpdateQty(item.menuItem.id, -1) },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)) // gray-200
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
    // 결과에 따른 테마 색상 설정
    val themeColor = when (result) {
        "fail" -> Color(0xFFDC2626) // red-600
        else -> Color(0xFF16A34A)   // green-600 (success or complete)
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

            // 결과 아이콘
            Surface(
                shape = CircleShape,
                color = themeColor,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(resultIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 결과 메시지
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
                color = Color(0xFF4B5563), // gray-600
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 실패 시 미션 리마인드 카드
            if (result == "fail" && mission != null) {
                KioskCard(
                    backgroundColor = Color(0xFFFEFCE8), // yellow-50
                    borderColor = Color(0xFFFEF08A),     // yellow-200
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("미션", fontSize = 16.sp, color = Color(0xFF854D0E), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp)) // yellow-800
                        Text(mission.text, fontSize = 18.sp, color = Color(0xFF713F12)) // yellow-900
                    }
                }
            }

            // 영수증 카드
            KioskCard(
                backgroundColor = Color(0xFFF9FAFB), // gray-50
                borderColor = Color(0xFFE5E7EB),     // gray-200
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("주문 내역", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                    cart.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.menuItem.name} × ${item.quantity}", fontSize = 18.sp, color = Color(0xFF374151))
                            Text(
                                "${NumberFormat.getNumberInstance(Locale.KOREA).format(item.menuItem.price * item.quantity)}원",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
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

            // 하단 버튼
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