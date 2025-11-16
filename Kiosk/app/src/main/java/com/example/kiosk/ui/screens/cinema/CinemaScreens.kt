// app/src/main/java/com/example/kiosk/ui/screens/cinema/CinemaScreens.kt
package com.example.kiosk.ui.screens.cinema

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.ui.components.KioskButton
import com.example.kiosk.ui.components.KioskCard
import java.text.NumberFormat
import java.util.Locale

// --- [추가] 이미지 관련 import ---
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.kiosk.R // 안드로이드 R 리소스
// --- [끝] ---

// ------------------------------------------------------------
// 연습 모드 시작 화면
// ------------------------------------------------------------
@Composable
fun PracticeWelcomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👋", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("환영합니다!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "영화 예매 연습을 시작합니다\n주문을 시작하려면\n아래 버튼을 눌러주세요",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 18.sp,
            lineHeight = 26.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onStart,
            modifier = Modifier
                .height(64.dp)
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("시작하기", fontSize = 24.sp)
        }
    }
}

// ------------------------------------------------------------
// HOME 화면
// ------------------------------------------------------------
@Composable
fun CinemaHomeScreen(
    onTicket: () -> Unit,
    onPrint: () -> Unit,
    onRefund: () -> Unit,
    onSnack: () -> Unit
) {
    val items = listOf(
        "🎟️ 티켓 구매" to onTicket,
        "🧾 예매티켓 출력" to onPrint,
        "↩️ 환불" to onRefund,
        "🍿 음식" to onSnack
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { (label, action) ->
            ElevatedCard(onClick = action) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


// ------------------------------------------------------------
// BOOKING (영화 → 시간 → 영화관+인원)
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    bookingStep: BookingStep,
    onChangeStep: (BookingStep) -> Unit,
    bookingDateMillis: Long,
    onChangeDate: (Long) -> Unit,
    movies: List<MovieItem>,
    theaters: List<TheaterOption>,
    selectedMovie: MovieItem?,
    onTapPoster: (MovieItem) -> Unit,
    selectedTime: String?,
    onSelectTime: (String) -> Unit,
    selectedTheater: TheaterOption?,
    onSelectTheater: (TheaterOption) -> Unit,
    // 인원 정보
    peopleCount: Int,
    adultCount: Int,
    childCount: Int,
    seniorCount: Int,
    onAdultInc: () -> Unit,
    onAdultDec: () -> Unit,
    onChildInc: () -> Unit,
    onChildDec: () -> Unit,
    onSeniorInc: () -> Unit,
    onSeniorDec: () -> Unit,

    onNextToSeat: () -> Unit,
    onBack: () -> Unit,
    onShowTimetable: () -> Unit,
    totalPrice: Int
) {
    var datePickerOpen by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = bookingDateMillis)
    val dateText = remember(bookingDateMillis) {
        java.text.SimpleDateFormat("yyyy.MM.dd (E)", java.util.Locale.KOREA).format(bookingDateMillis)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { datePickerOpen = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("예매일 변경 • $dateText")
            }
            OutlinedButton(
                onClick = onShowTimetable,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) { Text("상영시간표 보기") }
        }

        when (bookingStep) {
            BookingStep.MOVIE -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies) { movie ->
                        MovieCardCompact(
                            movie = movie,
                            selected = movie.id == selectedMovie?.id,
                            onClickPoster = { onTapPoster(movie) }
                        )
                    }
                }
            }

            BookingStep.TIME -> {
                Column(Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)) {
                    if (selectedMovie == null) {
                        Text("먼저 영화를 선택해주세요.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                            items(selectedMovie.showTimes.size) { i ->
                                val t = selectedMovie.showTimes[i]
                                val isSel = selectedTime == t
                                AssistChip(
                                    onClick = { onSelectTime(t) },
                                    label = { Text(t) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (isSel) Color(0xFF2563EB) else Color(0xFFF3F4F6),
                                        labelColor = if (isSel) Color.White else Color(0xFF374151)
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Row(Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { onChangeStep(BookingStep.MOVIE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) { Text("이전") }
                            val canNext = selectedTime != null
                            KioskButton(
                                onClick = { onChangeStep(BookingStep.THEATER_PEOPLE) },
                                enabled = canNext,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) { Text("다음") }
                        }
                    }
                }
            }

            BookingStep.THEATER_PEOPLE -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("영화관 선택", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(theaters.size) { idx ->
                            val t = theaters[idx]
                            TheaterCard(
                                theater = t,
                                selected = selectedTheater?.id == t.id,
                                onClick = { onSelectTheater(t) }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("인원 선택", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    val canPick = (selectedMovie != null && selectedTime != null && selectedTheater != null)

                    PeopleSelectView(
                        enabled = canPick,
                        adultCount = adultCount,
                        childCount = childCount,
                        seniorCount = seniorCount,
                        onAdultInc = onAdultInc,
                        onAdultDec = onAdultDec,
                        onChildInc = onChildInc,
                        onChildDec = onChildDec,
                        onSeniorInc = onSeniorInc,
                        onSeniorDec = onSeniorDec,
                        onNextToSeat = onNextToSeat,
                        totalPeopleCount = peopleCount,
                        totalPrice = totalPrice
                    )

                    Spacer(Modifier.height(24.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onChangeStep(BookingStep.TIME) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) { Text("이전 (시간)") }
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) { Text("처음으로") }
                    }
                }
            }
        }

        if (datePickerOpen) {
            DatePickerDialog(
                onDismissRequest = { datePickerOpen = false },
                confirmButton = {
                    TextButton(onClick = {
                        val picked = dateState.selectedDateMillis
                        if (picked != null) onChangeDate(picked)
                        datePickerOpen = false
                    }) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { datePickerOpen = false }) { Text("취소") }
                }
            ) { DatePicker(state = dateState) }
        }
    }
}

// ------------------------------------------------------------
// 인원 선택 UI
// ------------------------------------------------------------
@Composable
private fun PeopleSelectView(
    enabled: Boolean,
    adultCount: Int,
    childCount: Int,
    seniorCount: Int,
    onAdultInc: () -> Unit,
    onAdultDec: () -> Unit,
    onChildInc: () -> Unit,
    onChildDec: () -> Unit,
    onSeniorInc: () -> Unit,
    onSeniorDec: () -> Unit,
    onNextToSeat: () -> Unit,
    totalPeopleCount: Int,
    totalPrice: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PeopleCounterRow(
            label = "성인",
            count = adultCount,
            onInc = onAdultInc,
            onDec = onAdultDec,
            enabled = enabled
        )
        PeopleCounterRow(
            label = "아이 (2,000원 할인)",
            count = childCount,
            onInc = onChildInc,
            onDec = onChildDec,
            enabled = enabled
        )
        PeopleCounterRow(
            label = "우대 (2,000원 할인)",
            count = seniorCount,
            onInc = onSeniorInc,
            onDec = onSeniorDec,
            enabled = enabled
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "티켓 가격:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (totalPeopleCount > 0 && enabled) Color.Black else Color.Gray
            )
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice)}원",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (totalPeopleCount > 0 && enabled) Color(0xFFD32F2F) else Color.Gray
            )
        }

        Spacer(Modifier.height(16.dp))

        KioskButton(
            onClick = onNextToSeat,
            enabled = enabled && totalPeopleCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("좌석 선택 (${totalPeopleCount}명)", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

// PeopleSelectView의 개별 행
@Composable
private fun PeopleCounterRow(
    label: String,
    count: Int,
    onInc: () -> Unit,
    onDec: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 18.sp, color = if (enabled) Color.Black else Color.Gray)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onDec, enabled = enabled && count > 0, modifier = Modifier.size(56.dp), contentPadding = PaddingValues(0.dp)) { Text("－", fontSize = 20.sp) }
            Text(
                "$count",
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = onInc, enabled = enabled, modifier = Modifier.size(56.dp), contentPadding = PaddingValues(0.dp)) { Text("＋", fontSize = 20.sp) }
        }
    }
}

// ------------------------------------------------------------
// 영화 카드
// ------------------------------------------------------------
@Composable
private fun MovieCardCompact(
    movie: MovieItem,
    selected: Boolean,
    onClickPoster: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF2563EB) else Color(0xFFE5E7EB)
    KioskCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor,
        onClick = onClickPoster
    ) {
        Column(Modifier.padding(10.dp)) {
            val imageResId = when (movie.posterName) {
                "포스터 1" -> R.drawable.poster_1
                "포스터 2" -> R.drawable.poster_2
                "포스터 3" -> R.drawable.poster_3
                "포스터 4" -> R.drawable.poster_4
                "포스터 5" -> R.drawable.poster_5
                "포스터 6" -> R.drawable.poster_6
                "포스터 7" -> R.drawable.poster_7
                "포스터 8" -> R.drawable.poster_8
                "포스터 9" -> R.drawable.poster_9
                else -> R.drawable.ic_launcher_background // 기본 이미지
            }

            Image(
                painter = painterResource(id = imageResId),
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E7EB)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(8.dp))
            Text(movie.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("${movie.runningTimeMin}분", fontSize = 12.sp, color = Color(0xFF6B7280))
        }
    }
}

// ------------------------------------------------------------
// 영화관 카드
// ------------------------------------------------------------
@Composable
private fun TheaterCard(
    theater: TheaterOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) Color(0xFF2563EB) else Color(0xFFE5E7EB)
    KioskCard(
        borderColor = border,
        onClick = onClick,
        modifier = Modifier.width(220.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(theater.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text("잔여 ${theater.remainingSeats} / ${theater.totalSeats}", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
    }
}

// ------------------------------------------------------------
// 좌석 선택
// ------------------------------------------------------------
@Composable
fun SeatSelectScreen(
    peopleCount: Int,
    selectedSeats: Set<String>,
    reservedSeats: Set<String>,
    onToggleSeat: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("좌석 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("선택 ${selectedSeats.size}/$peopleCount", color = Color(0xFF2563EB), fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) { Text("SCREEN", fontSize = 12.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(12.dp))

        SeatGridWithAisle(
            rows = ('A'..'J').map { it.toString() },
            leftCols = (1..6).toList(),
            rightCols = (7..12).toList(),
            selectedSeats = selectedSeats,
            reservedSeats = reservedSeats,
            onToggle = onToggleSeat,
            onReservedClick = {
                Toast.makeText(context, "이미 선택된 좌석입니다.", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(Modifier.weight(1f))
        Row(Modifier
            .fillMaxWidth()
            .padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier
                .weight(1f)
                .height(56.dp)) { Text("이전") }
            val enabled = selectedSeats.size == peopleCount
            KioskButton(onClick = onNext, enabled = enabled, modifier = Modifier
                .weight(1f)
                .height(56.dp)) { Text("결제하기") }
        }
    }
}

@Composable
private fun SeatGridWithAisle(
    rows: List<String>,
    leftCols: List<Int>,
    rightCols: List<Int>,
    selectedSeats: Set<String>,
    reservedSeats: Set<String>,
    onToggle: (String) -> Unit,
    onReservedClick: () -> Unit
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        rows.forEach { r ->
            Row(Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    leftCols.forEach { c ->
                        val code = "$r$c"
                        val isReserved = reservedSeats.contains(code)
                        SeatChip(
                            code = code,
                            isReserved = isReserved,
                            isSelected = selectedSeats.contains(code),
                            onClick = {
                                if (isReserved) onReservedClick() else onToggle(code)
                            }
                        )
                    }
                }
                Spacer(Modifier.width(24.dp)) // 통로
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rightCols.forEach { c ->
                        val code = "$r$c"
                        val isReserved = reservedSeats.contains(code)
                        SeatChip(
                            code = code,
                            isReserved = isReserved,
                            isSelected = selectedSeats.contains(code),
                            onClick = {
                                if (isReserved) onReservedClick() else onToggle(code)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatChip(
    code: String,
    isReserved: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        isReserved -> Color(0xFF6B7280)
        isSelected -> Color(0xFF2563EB)
        else -> Color(0xFFF3F4F6)
    }

    val fg = if (isSelected || isReserved) Color.White else Color(0xFF111827)

    Surface(
        modifier = Modifier
            .size(28.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) { Box(contentAlignment = Alignment.Center) { Text(code, fontSize = 10.sp, color = fg) } }
}

// ------------------------------------------------------------
// ✅ 예매 티켓 출력 화면 (로직 수정)
// ------------------------------------------------------------
@Composable
fun PrintTicketScreen(
    onBack: () -> Unit
) {
    // --- 데이터 로드 ---
    val movies = rememberMovies()
    val theaters = rememberTheaters()
    val bookedTickets = rememberBookedTickets(movies, theaters) // 예매된 티켓 Map

    // --- 다이얼로그 상태 관리 ---
    var showInputDialog by remember { mutableStateOf(false) }
    var foundTicket by remember { mutableStateOf<BookedTicket?>(null) } // 성공 팝업
    var showErrorDialog by remember { mutableStateOf(false) }     // 실패 팝업

    // --- ✅ [요청] 티켓 출력 상태 ---
    var isPrinting by remember { mutableStateOf(false) }

    if (isPrinting) {
        // --- ✅ [요청] 1. 티켓 출력 중 화면 ---
        TicketPrintingScreen(
            onDone = onBack,  // onBack은 CinemaFlowRoot에서 resetFlow()와 연결됨
            onAgain = onBack  // onBack은 CinemaFlowRoot에서 resetFlow()와 연결됨
        )
    } else {
        // --- 2. 티켓 조회 메인 화면 ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                tint = Color(0xFF374151)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "QR코드를 입력해주세요\n혹은 예매 번호를 입력해주세요",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
            Spacer(Modifier.height(40.dp))

            // --- 예매 번호 입력 버튼 ---
            KioskButton(
                onClick = { showInputDialog = true },
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
            ) {
                Text("예매 번호로 찾기", fontSize = 18.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
            ) {
                Text("홈으로 돌아가기", fontSize = 18.sp)
            }
        }
    }


    // --- 다이얼로그 로직 ---

    // 1. 번호 입력 팝업
    if (showInputDialog) {
        BookingNumberInputDialog(
            onDismiss = { showInputDialog = false },
            onConfirm = { number ->
                showInputDialog = false
                // 입력된 번호로 티켓 조회
                val ticket = bookedTickets[number]
                if (ticket != null) {
                    foundTicket = ticket // 찾았으면 성공 팝업
                } else {
                    showErrorDialog = true // 못 찾았으면 실패 팝업
                }
            }
        )
    }

    // 2. 조회 성공 팝업 (영수증)
    if (foundTicket != null) {
        BookingResultDialog(
            ticket = foundTicket!!,
            movie = movies.find { it.id == foundTicket!!.movieId },
            theater = theaters.find { it.id == foundTicket!!.theaterId },
            onDismiss = { foundTicket = null },
            // ✅ [요청] "실물 티켓 출력" 버튼 클릭 시
            onPrintTicket = {
                foundTicket = null // 팝업 닫기
                isPrinting = true  // 출력 화면으로 전환
            }
        )
    }

    // 3. 조회 실패 팝업
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("조회 실패") },
            text = { Text("일치하는 예매 내역이 없습니다.\n(테스트 번호: 112233445566)") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}

// ------------------------------------------------------------
// ✅ [요청] 티켓 출력 완료 화면 (신규 추가)
// ------------------------------------------------------------
@Composable
private fun TicketPrintingScreen(
    onDone: () -> Unit,
    onAgain: () -> Unit
) {
    val themeColor = Color(0xFF16A34A) // Green (결제 완료와 동일한 테마)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // 세로 중앙 정렬
    ) {
        // 결과 아이콘
        Surface(
            shape = CircleShape,
            color = themeColor,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 결과 메시지
        Text(
            text = "출력 완료!", // "결제 완료!" -> "출력 완료!"
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "실물 티켓이 출력됩니다.\n티켓을 확인해주세요.", // 문구 수정
            fontSize = 18.sp,
            color = Color(0xFF4B5563), // gray-600
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 하단 버튼 (결제 완료와 동일)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAgain, modifier = Modifier.height(52.dp)) { Text("다시 홈으로") }
            KioskButton(onClick = onDone, modifier = Modifier.height(52.dp)) { Text("완료 (종료)") }
        }
    }
}