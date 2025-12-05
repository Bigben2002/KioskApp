package com.example.kiosk.ui.screens.restaurant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale
import com.example.kiosk.data.model.MenuItem
import com.example.kiosk.data.model.CartItem
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.kiosk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantKioskScreen(
    isPractice: Boolean,
    onBack: () -> Unit,
    viewModel: RestaurantKioskViewModel = viewModel(),
    onStartPayment: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()
    val practiceStep by viewModel.practiceStep.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showOptionDialog by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showCartDialog by remember { mutableStateOf(false) }

    val themeColor = Color(0xFF8B4513)

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

    // 장바구니 다이얼로그
    if (showCartDialog) {
        CartDialog(
            cart = cart,
            totalPrice = totalPrice,
            themeColor = themeColor,
            onDismiss = { showCartDialog = false },
            onUpdateQty = { itemId, delta ->
                viewModel.updateQuantity(itemId, delta)
            },
            onCheckout = {
                android.util.Log.d("RestaurantKiosk", "CartDialog onCheckout 호출됨")
                showCartDialog = false
                android.util.Log.d("RestaurantKiosk", "onStartPayment 호출 시작")
                onStartPayment()
                android.util.Log.d("RestaurantKiosk", "onStartPayment 호출 완료")
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
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text("총 금액", fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = { showCartDialog = true },
                        modifier = Modifier.height(70.dp).width(210.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        enabled = cart.isNotEmpty()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("결제하기", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(10.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${cart.sumOf { it.quantity }}",
                                        color = themeColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                    .width(100.dp)
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

            // 중앙: 메뉴 그리드
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White)
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
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredMenu) { item ->
                        MenuItemCard(
                            item = item,
                            themeColor = themeColor,
                            onClick = {
                                if (item.category == "국밥류" && item.options.isNotEmpty()) {
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
            .height(56.dp)
            .padding(horizontal = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            category,
            fontSize = 15.sp,
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
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    guideText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0D47A1)
                )
            }

            if (step == 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("시작하기", fontSize = 14.sp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 메뉴 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = when (item.name) {
                    "돼지국밥" -> R.drawable.dwaeji_gukbap
                    "순대국밥" -> R.drawable.sundae_gukbap
                    "뚝배기불고기" -> R.drawable.ttukbaegi_bulgogi
                    "육개장" -> R.drawable.yukgaejang
                    "뼈해장국" -> R.drawable.ppyeo_haejangguk
                    "순대 모듬" -> R.drawable.assorted_sundae
                    "수육 (小)", "수육 (中)", "수육 (大)" -> R.drawable.sooyuk
                    "모듬" -> R.drawable.assorted_sundae_sooyuk
                    "김치" -> R.drawable.kimchi
                    "소주" -> R.drawable.soju
                    "맥주" -> R.drawable.beer
                    "콜라" -> R.drawable.cola
                    "사이다" -> R.drawable.cider
                    "탄산수" -> R.drawable.sparkling_water
                    else -> null
                }

                if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        when (item.category) {
                            "국밥류" -> "🍜"
                            "사이드" -> "🥓"
                            else -> "🍺"
                        },
                        fontSize = 56.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 메뉴 이름
            Text(
                item.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            // 가격
            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(item.price)}원",
                fontSize = 16.sp,
                color = themeColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CartDialog(
    cart: List<CartItem>,
    totalPrice: Int,
    themeColor: Color,
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
                        items(cart.size) { index ->
                            CartItemRow(
                                cartItem = cart[index],
                                onQuantityChange = { delta ->
                                    onUpdateQty(cart[index].menuItem.id, delta)
                                },
                                themeColor = themeColor
                            )
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
                        color = themeColor
                    )
                }

                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = cart.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("결제하기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cartItem.menuItem.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            if (cartItem.selectedOption != null && cartItem.selectedOption.price > 0) {
                val options = cartItem.selectedOption.name.split(", ")
                options.forEach { opt ->
                    if (!opt.contains("보통") && !opt.contains("수육 없음")) {
                        Text(
                            "  • $opt",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            Text(
                "${NumberFormat.getNumberInstance(Locale.KOREA).format(
                    (cartItem.menuItem.price + (cartItem.selectedOption?.price ?: 0)) * cartItem.quantity
                )}원",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onQuantityChange(-1) },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "감소", modifier = Modifier.size(16.dp))
            }

            Text(
                "${cartItem.quantity}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.widthIn(min = 24.dp),
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                onClick = { onQuantityChange(1) },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "증가", modifier = Modifier.size(16.dp))
            }
        }
    }
}