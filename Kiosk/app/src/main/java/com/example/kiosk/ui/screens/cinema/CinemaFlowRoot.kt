// app/src/main/java/com/example/kiosk/ui/screens/cinema/CinemaFlowRoot.kt
package com.example.kiosk.ui.screens.cinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaFlowRoot(
    isPracticeMode: Boolean,
    onExit: () -> Unit
) {
    // --- 상태 관리 ---
    var stage by remember { mutableStateOf(CinemaStage.HOME) }
    var bookingStep by remember { mutableStateOf(BookingStep.MOVIE) }
    var practiceStarted by remember { mutableStateOf(!isPracticeMode) }

    val todayMillis = remember { System.currentTimeMillis() }
    var bookingDateMillis by remember { mutableStateOf(todayMillis) }
    var selectedMovie by remember { mutableStateOf<MovieItem?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedTheater by remember { mutableStateOf<TheaterOption?>(null) }

    var adultCount by remember { mutableStateOf(0) }
    var childCount by remember { mutableStateOf(0) }
    var seniorCount by remember { mutableStateOf(0) }
    val totalPeopleCount by derivedStateOf { adultCount + childCount + seniorCount }

    var selectedSeats by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showTimetableDialog by remember { mutableStateOf(false) }

    var showSeatInstructionPopup by remember { mutableStateOf(false) }

    // 결제 단계
    var paymentStep by remember { mutableStateOf(PaymentStep.METHOD_SELECT) }

    val totalPrice by derivedStateOf {
        val fullPrice = when {
            selectedTheater?.name?.contains("4DX") == true -> 16000
            selectedTheater?.name?.contains("IMAX") == true -> 16000
            else -> 10000
        }
        val childPrice = (fullPrice - 2000).coerceAtLeast(0)
        val seniorPrice = (fullPrice - 2000).coerceAtLeast(0)

        (adultCount * fullPrice) + (childCount * childPrice) + (seniorCount * seniorPrice)
    }

    val barColor = Color(0xFF334155)

    @Composable
    fun PracticeBanner(text: String) {
        if (isPracticeMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2563EB))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) { Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        }
    }

    // 모든 상태 초기화
    fun resetFlow() {
        stage = CinemaStage.HOME
        bookingStep = BookingStep.MOVIE
        practiceStarted = !isPracticeMode
        bookingDateMillis = todayMillis
        selectedMovie = null
        selectedTime = null
        selectedTheater = null
        adultCount = 0
        childCount = 0
        seniorCount = 0
        selectedSeats = emptySet()
        paymentStep = PaymentStep.METHOD_SELECT
        showSeatInstructionPopup = false
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("영화관", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.White)
                    }
                },
                actions = {
                    if (stage != CinemaStage.HOME) {
                        IconButton(onClick = { resetFlow() }) {
                            Icon(Icons.Default.Home, contentDescription = "홈", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = barColor)
            )
        }
    ) { inner ->
        Column(modifier = Modifier.padding(inner).fillMaxSize()) {

            // --- 화면(Stage) 분기 ---
            when (stage) {
                // --- 1. 홈 ---
                CinemaStage.HOME -> {
                    PracticeBanner("수행할 작업을 선택해주세요 (예: 티켓 구매)")
                    CinemaHomeScreen( // ✅ CinemaHome -> CinemaHomeScreen 으로 변경
                        onTicket = { stage = CinemaStage.BOOKING },
                        onPrint  = { stage = CinemaStage.PRINT }, // ✅ [요청 2] PRINT 스크린으로 이동
                        onRefund = {},
                        onSnack  = { stage = CinemaStage.SNACK }
                    )
                }

                // --- 2. 예매 ---
                CinemaStage.BOOKING -> {
                    if (isPracticeMode && !practiceStarted) {
                        PracticeBanner("영화 예매 연습을 시작합니다")
                        PracticeWelcomeScreen(onStart = { practiceStarted = true })
                    } else {
                        val bannerText = when (bookingStep) {
                            BookingStep.MOVIE -> "관람을 원하시는 영화를 선택해주세요"
                            BookingStep.TIME -> "관람하실 시간을 선택해주세요"
                            BookingStep.THEATER_PEOPLE -> "관람하실 상영관과 인원을 선택해주세요"
                        }
                        PracticeBanner(bannerText)

                        BookingScreen(
                            bookingStep = bookingStep,
                            onChangeStep = { bookingStep = it },
                            bookingDateMillis = bookingDateMillis,
                            onChangeDate = { bookingDateMillis = it },
                            movies = rememberMovies(),
                            theaters = rememberTheaters(),
                            selectedMovie = selectedMovie,
                            onTapPoster = { movie ->
                                selectedMovie = movie
                                selectedTime = null
                                selectedTheater = null
                                bookingStep = BookingStep.TIME
                            },
                            selectedTime = selectedTime,
                            onSelectTime = { selectedTime = it },
                            selectedTheater = selectedTheater,
                            onSelectTheater = { selectedTheater = it },

                            peopleCount = totalPeopleCount,
                            adultCount = adultCount,
                            childCount = childCount,
                            seniorCount = seniorCount,
                            onAdultInc = { if (totalPeopleCount < 8) adultCount++ },
                            onAdultDec = { if (adultCount > 0) adultCount-- },
                            onChildInc = { if (totalPeopleCount < 8) childCount++ },
                            onChildDec = { if (childCount > 0) childCount-- },
                            onSeniorInc = { if (totalPeopleCount < 8) seniorCount++ },
                            onSeniorDec = { if (seniorCount > 0) seniorCount-- },

                            onNextToSeat = {
                                stage = CinemaStage.SEAT
                                showSeatInstructionPopup = true
                            },
                            onBack = { stage = CinemaStage.HOME },
                            onShowTimetable = { showTimetableDialog = true },
                            totalPrice = totalPrice
                        )

                        if (showTimetableDialog) {
                            TimetableDialog(
                                movies = rememberMovies(),
                                onDismiss = { showTimetableDialog = false }
                            )
                        }
                    }
                }

                // --- 3. 좌석 선택 ---
                CinemaStage.SEAT -> {
                    PracticeBanner("선택한 인원 수(${totalPeopleCount}명)만큼 좌석을 선택해주세요")

                    val reservedSeats = rememberReservedSeats(selectedTheater?.id)

                    SeatSelectScreen(
                        peopleCount = totalPeopleCount,
                        selectedSeats = selectedSeats,
                        reservedSeats = reservedSeats,
                        onToggleSeat = { seat ->
                            selectedSeats = if (selectedSeats.contains(seat)) {
                                selectedSeats - seat
                            } else {
                                if (selectedSeats.size < totalPeopleCount) selectedSeats + seat else selectedSeats
                            }
                        },
                        onNext = { stage = CinemaStage.PAYMENT },
                        onBack = { stage = CinemaStage.BOOKING }
                    )

                    if (showSeatInstructionPopup) {
                        SeatInstructionDialog(
                            onDismiss = { showSeatInstructionPopup = false }
                        )
                    }
                }

                // --- 4. 결제 단계 ---
                CinemaStage.PAYMENT -> {
                    when (paymentStep) {
                        PaymentStep.METHOD_SELECT -> {
                            PracticeBanner("결제 방식을 선택하세요 (예: 카드 결제)")
                            PaymentMethodSelectScreen(
                                // ✅ [요청 1] 선택된 method에 따라 분기
                                onPaid = { method ->
                                    if (method == "CARD") {
                                        paymentStep = PaymentStep.CARD_INSERT
                                    } else if (method == "QR") {
                                        paymentStep = PaymentStep.QR_SCAN
                                    }
                                },
                                onBack = { stage = CinemaStage.SEAT }
                            )
                        }
                        PaymentStep.CARD_INSERT -> {
                            PracticeBanner("화면의 안내에 따라 카드를 삽입해주세요")
                            PaymentCardInsertScreen()
                            LaunchedEffect(Unit) {
                                delay(2000)
                                paymentStep = PaymentStep.PROCESSING
                            }
                        }
                        // ✅ [요청 1] QR 스캔 단계 추가
                        PaymentStep.QR_SCAN -> {
                            PracticeBanner("화면의 안내에 따라 QR코드를 스캔해주세요")
                            PaymentQrScanScreen()
                            LaunchedEffect(Unit) {
                                delay(2000) // QR 스캔 대기 시간
                                paymentStep = PaymentStep.PROCESSING
                            }
                        }
                        PaymentStep.PROCESSING -> {
                            PracticeBanner("결제 중입니다. 잠시만 기다려주세요...")
                            PaymentProcessingScreen()
                            LaunchedEffect(Unit) {
                                delay(3000)
                                paymentStep = PaymentStep.SUCCESS
                            }
                        }
                        PaymentStep.SUCCESS -> {
                            PracticeBanner("결제가 완료되었습니다!")
                            PaymentSuccessScreen_Ticket(
                                movie = selectedMovie,
                                time = selectedTime,
                                theater = selectedTheater,
                                seats = selectedSeats.toList().sorted(),
                                dateMillis = bookingDateMillis,
                                adultCount = adultCount,
                                childCount = childCount,
                                seniorCount = seniorCount,
                                totalPrice = totalPrice,
                                onDone = onExit,
                                onAgain = { resetFlow() }
                            )
                        }
                    }
                }

                // --- 5. 스낵 ---
                CinemaStage.SNACK -> {
                    PracticeBanner("주문할 스낵이나 음료를 선택해주세요")
                    CinemaFoodScreen(
                        modifier = Modifier.fillMaxSize(),
                        onClose = { stage = CinemaStage.HOME }
                    )
                }

                // ✅ [요청 2] 티켓 출력 단계 추가
                CinemaStage.PRINT -> {
                    PracticeBanner("예매하신 티켓의 QR/예매번호를 입력해주세요")
                    PrintTicketScreen(
                        onBack = { resetFlow() }
                    )
                }
            }
        }
    }
}