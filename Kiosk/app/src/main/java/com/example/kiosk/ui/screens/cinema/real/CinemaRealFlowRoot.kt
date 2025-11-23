package com.example.kiosk.ui.screens.cinema.real

import androidx.compose.runtime.derivedStateOf
import android.app.Application
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
import com.example.kiosk.ui.screens.cinema.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaRealFlowRoot(
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val application = remember(context) { context.applicationContext as Application }
    val historyRepository = remember { HistoryRepository(application) }
    val coroutineScope = rememberCoroutineScope()

    // --- 1. 미션 로드 및 상태 관리 ---
    val allMissions = rememberCinemaMissions()
    // ✅ [수정] 미션을 갱신할 수 있도록 mutableStateOf로 변경
    var currentMission by remember { mutableStateOf(allMissions.random()) }

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
    val totalPeopleCount by remember {
        derivedStateOf { adultCount + childCount + seniorCount }
    }

    var selectedSeats by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSeatInstructionPopup by remember { mutableStateOf(false) }

    var paymentStep by remember { mutableStateOf(PaymentStep.METHOD_SELECT) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) } // ✅ 선택된 결제 방식 저장

    // 예매 조건 충족 여부 (결제 전까지의 내용)
    var bookingConditionsMet by remember { mutableStateOf(false) }

    // 최종 미션 결과 텍스트
    var finalMissionResultText by remember { mutableStateOf<String?>(null) }

    val totalPrice by remember {
        derivedStateOf {
            val fullPrice = when {
                selectedTheater?.name?.contains("4DX") == true -> 16000
                selectedTheater?.name?.contains("IMAX") == true -> 16000
                else -> 10000
            }
            val childPrice = (fullPrice - 2000).coerceAtLeast(0)
            val seniorPrice = (fullPrice - 2000).coerceAtLeast(0)

            (adultCount * fullPrice) + (childCount * childPrice) + (seniorCount * seniorPrice)
        }
    }

    val barColor = Color(0xFF334155)

    // ⬇️ 미션 체크 로직 (결제 방식 포함)
    fun checkTicketMission(
        mission: RequiredTicketMission,
        movieId: String?, time: String?, theaterId: String?,
        adultCount: Int, childCount: Int, seniorCount: Int,
        paymentMethod: String? // ✅ 파라미터 추가
    ): Boolean {
        return (
                movieId == mission.requiredMovieId &&
                        time == mission.requiredTime &&
                        theaterId == mission.requiredTheaterId &&
                        adultCount == mission.requiredAdult &&
                        childCount == mission.requiredChild &&
                        seniorCount == mission.requiredSenior &&
                        paymentMethod == mission.requiredPaymentMethod // ✅ 결제 방식 체크
                )
    }

    suspend fun saveMissionResult(
        isSuccess: Boolean
    ): String = withContext(Dispatchers.IO) {
        val resultText = if (isSuccess) "100%" else "0%"
        val dateFormat = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
        val record = HistoryRecord(
            id = System.currentTimeMillis().toString(),
            date = dateFormat.format(Date()),
            mission = currentMission.title,
            success = isSuccess,
            userOrder = emptyList(),
            timestamp = System.currentTimeMillis(),
        )
        historyRepository.saveHistory(record)
        return@withContext resultText
    }

    // ✅ [수정] 모든 상태 초기화 및 **새로운 미션 할당**
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
        selectedPaymentMethod = null
        bookingConditionsMet = false
        finalMissionResultText = null

        // 새로운 미션 랜덤 할당
        currentMission = allMissions.random()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("영화관 실전 모드", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = Color.White
                        )
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
        Column(modifier = Modifier
            .padding(inner)
            .fillMaxSize()) {

            // === 미션 안내 배너 ===
            // ✅ 결제 방식이 미션에 포함되었음을 강조
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEA580C))
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

            when (stage) {
                CinemaStage.HOME -> {
                    CinemaHomeScreen(
                        onTicket = { stage = CinemaStage.BOOKING },
                        onPrint = { stage = CinemaStage.PRINT },
                        onRefund = {},
                        onSnack = { stage = CinemaStage.SNACK }
                    )
                }

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
                        onShowTimetable = { },
                        totalPrice = totalPrice
                    )
                }

                CinemaStage.SEAT -> {
                    // ✅ 변경된 rememberReservedSeats 사용 (랜덤 점유)
                    val reservedSeats = rememberReservedSeats(selectedTheater)

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
                            // 결제 전 단계까지의 미션 조건 임시 저장 (결제 방식 제외)
                            // 결제 방식은 다음 단계에서 선택하므로 여기서는 나머지 조건만 확인
                            // 실제 최종 확인은 결제 완료 시점에 수행
                            stage = CinemaStage.PAYMENT
                        },
                        onBack = { stage = CinemaStage.BOOKING }
                    )

                    if (showSeatInstructionPopup) {
                        SeatInstructionDialog(onDismiss = { showSeatInstructionPopup = false })
                    }
                }

                CinemaStage.PAYMENT -> {
                    when (paymentStep) {
                        PaymentStep.METHOD_SELECT -> {
                            PaymentMethodSelectScreen(
                                onPaid = { method ->
                                    selectedPaymentMethod = method // ✅ 결제 방식 저장
                                    if (method == "CARD") paymentStep = PaymentStep.CARD_INSERT
                                    else if (method == "QR") paymentStep = PaymentStep.QR_SCAN
                                },
                                onBack = { stage = CinemaStage.SEAT }
                            )
                        }

                        PaymentStep.CARD_INSERT -> {
                            PaymentCardInsertScreen()
                            LaunchedEffect(Unit) {
                                delay(2000); paymentStep = PaymentStep.PROCESSING
                            }
                        }

                        PaymentStep.QR_SCAN -> {
                            PaymentQrScanScreen()
                            LaunchedEffect(Unit) {
                                delay(2000); paymentStep = PaymentStep.PROCESSING
                            }
                        }

                        PaymentStep.PROCESSING -> {
                            PaymentProcessingScreen()
                            LaunchedEffect(Unit) {
                                delay(3000)

                                // ✅ 최종 미션 성공 여부 판별 (결제 방식까지 포함)
                                val isSuccess = checkTicketMission(
                                    currentMission,
                                    selectedMovie?.id, selectedTime, selectedTheater?.id,
                                    adultCount, childCount, seniorCount,
                                    selectedPaymentMethod
                                )

                                finalMissionResultText = saveMissionResult(isSuccess)

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
                                missionResultText = finalMissionResultText ?: "판독 중...",
                                onDone = onExit,
                                // ✅ 다시 도전 시 resetFlow() 호출 -> 새로운 미션 생성됨
                                onAgain = { resetFlow() }
                            )
                        }
                    }
                }

                CinemaStage.SNACK -> {
                    CinemaFoodScreen(
                        modifier = Modifier.fillMaxSize(),
                        onClose = { stage = CinemaStage.HOME }
                    )
                }

                CinemaStage.PRINT -> {
                    PrintTicketScreen(onBack = { stage = CinemaStage.HOME })
                }
            }
        }
    }
}