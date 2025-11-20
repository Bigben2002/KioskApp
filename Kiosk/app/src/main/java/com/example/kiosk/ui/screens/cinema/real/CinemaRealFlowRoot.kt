package com.example.kiosk.ui.screens.cinema.real

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk.data.model.HistoryRecord
import com.example.kiosk.data.model.RequiredItem
import com.example.kiosk.data.repository.HistoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.kiosk.ui.screens.cinema.* // CinemaData, Screens의 모든 요소를 임포트

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaRealFlowRoot(
    onExit: () -> Unit
) {
    // ⬇️ KioskViewModel 역할 흡수: 상태 관리 및 리포지토리 초기화
    val context = LocalContext.current
    val historyRepository = remember { HistoryRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // --- 1. 미션 로드 및 상태 관리 ---
    val allMissions = rememberCinemaMissions()
    val currentMission = remember { allMissions.random() } // ✅ 미션 랜덤 선택

    // --- 2. 상태 변수 ---
    var stage by remember { mutableStateOf(CinemaStage.HOME) }
    var bookingStep by remember { mutableStateOf(BookingStep.MOVIE) }

    val todayMillis = remember { System.currentTimeMillis() }
    var bookingDateMillis by remember { mutableStateOf(todayMillis) }
    var selectedMovie by remember { mutableStateOf<MovieItem?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedTheater by remember { mutableStateOf<TheaterOption?>(null) }

    var adultCount by remember { mutableIntStateOf(0) }
    var childCount by remember { mutableIntStateOf(0) }
    var seniorCount by remember { mutableIntStateOf(0) }
    val totalPeopleCount by derivedStateOf { adultCount + childCount + seniorCount }

    var selectedSeats by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSeatInstructionPopup by remember { mutableStateOf(false) }

    // 결제 단계
    var paymentStep by remember { mutableStateOf(PaymentStep.METHOD_SELECT) }

    // 미션 성공 상태
    var ticketMissionSuccess by remember { mutableStateOf(false) }
    var finalMissionResultText by remember { mutableStateOf<String?>(null) }

    // 티켓 가격 계산 (기존 로직 유지)
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


    // ⬇️ KioskViewModel 역할 흡수: 비즈니스 로직

    /**
     * KioskViewModel의 checkTicketMission 역할 흡수
     */
    fun checkTicketMission(
        mission: RequiredTicketMission,
        movieId: String?, time: String?, theaterId: String?,
        adultCount: Int, childCount: Int, seniorCount: Int
    ): Boolean {
        return (
                movieId == mission.requiredMovieId &&
                        time == mission.requiredTime &&
                        theaterId == mission.requiredTheaterId &&
                        adultCount == mission.requiredAdult &&
                        childCount == mission.requiredChild &&
                        seniorCount == mission.requiredSenior
                )
    }

    /**
     * KioskViewModel의 getAndSaveCinemaMissionResult 역할 흡수
     */
    suspend fun saveMissionResult(
        isTicketSuccess: Boolean
    ): String = withContext(Dispatchers.IO) {

        val totalMissions = 1
        var successCount = if (isTicketSuccess) 1 else 0

        val successStatus = if (isTicketSuccess) "100%" else "0%"
        val totalSuccess = successCount == totalMissions
        val resultText = "$successCount/$totalMissions ($successStatus)"

        // HistoryRecord 저장
        val dateFormat = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
        val record = HistoryRecord(
            id = System.currentTimeMillis().toString(),
            date = dateFormat.format(Date()),
            mission = currentMission.title,
            success = totalSuccess,
            userOrder = emptyList(),
            timestamp = System.currentTimeMillis(),
            cinemaSuccessStatus = resultText
        )
        historyRepository.saveHistory(record)
        return@withContext resultText
    }


    // 모든 상태 초기화
    fun resetFlow() {
        stage = CinemaStage.HOME
        bookingStep = BookingStep.MOVIE
        bookingDateMillis = todayMillis
        selectedMovie = null
        selectedTime = null
        selectedTheater = null
        adultCount = 0
        childCount = 0
        seniorCount = 0
        selectedSeats = emptySet()
        paymentStep = PaymentStep.METHOD_SELECT
        ticketMissionSuccess = false
        finalMissionResultText = null
    }


    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("영화관 실전 모드", color = Color.White, fontWeight = FontWeight.Bold) },
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

            // === 1. 미션 안내 배너 (실전 모드) ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEA580C)) // orange-600
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🎯 미션: ${currentMission.title}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // --- 화면(Stage) 분기 ---
            when (stage) {
                // --- 1. 홈 ---
                CinemaStage.HOME -> {
                    CinemaHomeScreen(
                        onTicket = { stage = CinemaStage.BOOKING },
                        onPrint  = { stage = CinemaStage.PRINT },
                        onRefund = {},
                        onSnack  = { stage = CinemaStage.SNACK }
                    )
                }

                // --- 2. 예매 ---
                CinemaStage.BOOKING -> {
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
                        onShowTimetable = { /* 실전 모드에서는 팝업 없음 */ },
                        totalPrice = totalPrice
                    )
                }

                // --- 3. 좌석 선택 ---
                CinemaStage.SEAT -> {
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
                        onNext = {
                            // ✅ 티켓 미션 성공 여부 검증 및 저장
                            ticketMissionSuccess = checkTicketMission(
                                currentMission,
                                selectedMovie?.id, selectedTime, selectedTheater?.id,
                                adultCount, childCount, seniorCount
                            )

                            // 바로 결제 단계로 이동
                            stage = CinemaStage.PAYMENT
                        },
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
                            PaymentMethodSelectScreen(
                                onPaid = { method ->
                                    if (method == "CARD") paymentStep = PaymentStep.CARD_INSERT
                                    else if (method == "QR") paymentStep = PaymentStep.QR_SCAN
                                },
                                onBack = { stage = CinemaStage.SEAT }
                            )
                        }
                        PaymentStep.CARD_INSERT -> {
                            PaymentCardInsertScreen()
                            LaunchedEffect(Unit) { delay(2000); paymentStep = PaymentStep.PROCESSING }
                        }
                        PaymentStep.QR_SCAN -> {
                            PaymentQrScanScreen()
                            LaunchedEffect(Unit) { delay(2000); paymentStep = PaymentStep.PROCESSING }
                        }
                        PaymentStep.PROCESSING -> {
                            PaymentProcessingScreen()
                            LaunchedEffect(Unit) {
                                delay(3000)

                                // ✅ 최종 미션 결과 저장 및 텍스트 획득 (코루틴 사용)
                                coroutineScope.launch {
                                    finalMissionResultText = saveMissionResult(
                                        isTicketSuccess = ticketMissionSuccess,
                                    )
                                }
                                paymentStep = PaymentStep.SUCCESS
                            }
                        }
                        PaymentStep.SUCCESS -> {
                            MissionResultScreen_Ticket(
                                movie = selectedMovie,
                                time = selectedTime,
                                theater = selectedTheater,
                                seats = selectedSeats.toList().sorted(),
                                dateMillis = bookingDateMillis,
                                adultCount = adultCount,
                                childCount = childCount,
                                seniorCount = seniorCount,
                                totalPrice = totalPrice,
                                missionResultText = finalMissionResultText ?: "오류 발생",
                                onDone = onExit,
                                onAgain = { resetFlow() }
                            )
                        }
                    }
                }

                // --- 5. 스낵 (미션과 무관한 독립된 기능) ---
                CinemaStage.SNACK -> {
                    CinemaFoodScreen(
                        modifier = Modifier.fillMaxSize(),
                        onClose = { stage = CinemaStage.HOME } // 완료 시 홈으로 복귀
                    )
                }

                // --- 6. 티켓 출력 ---
                CinemaStage.PRINT -> {
                    PrintTicketScreen(onBack = { stage = CinemaStage.HOME })
                }
            }
        }
    }
}