package com.example.kiosk.ui.screens.cinema

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.kiosk.data.model.RequiredItem

// ------------------------------------------------------------
// 스텝 정의 (기존 유지)
// ------------------------------------------------------------
enum class CinemaStage { HOME, BOOKING, SEAT, PAYMENT, SNACK, PRINT }
enum class BookingStep { MOVIE, TIME, THEATER_PEOPLE }
enum class PaymentStep { METHOD_SELECT, CARD_INSERT, QR_SCAN, PROCESSING, SUCCESS }
enum class FoodStep { MENU, PAYMENT }

// ------------------------------------------------------------
// 데이터 모델 (기존 유지)
// ------------------------------------------------------------
data class MovieItem(
    val id: String,
    val title: String,
    val posterName: String,
    val runningTimeMin: Int,
    val showTimes: List<String>
)

data class TheaterOption(
    val id: String,
    val name: String,
    val totalSeats: Int,
    val remainingSeats: Int
)

data class RequiredTicketMission(
    val id: Int,
    val title: String,
    val requiredMovieId: String,
    val requiredTime: String,
    val requiredTheaterId: String,
    val requiredAdult: Int,
    val requiredChild: Int,
    val requiredSenior: Int,
    val requiredPaymentMethod: String, // CARD / QR
    val requiredFood: List<RequiredItem>
)

data class BookedTicket(
    val bookingNumber: String,
    val movieId: String,
    val theaterId: String,
    val time: String,
    val seats: List<String>,
    val adultCount: Int,
    val childCount: Int,
    val seniorCount: Int
) {
    val totalPeople: Int get() = adultCount + childCount + seniorCount
}

// ------------------------------------------------------------
// 더미 데이터 (기존 유지)
// ------------------------------------------------------------
@Composable
fun rememberMovies(): List<MovieItem> = remember {
    listOf(
        MovieItem("m1", "인사이드 아웃 2", "포스터 1", 96, listOf("10:30", "13:00", "15:40")),
        MovieItem("m2", "범죄도시 4", "포스터 2", 109, listOf("09:50", "12:20", "17:00")),
        MovieItem("m3", "듄: 파트2", "포스터 3", 166, listOf("11:10", "14:30", "18:50")),
        MovieItem("m4", "웡카", "포스터 4", 116, listOf("10:00", "12:30", "15:00")),
        MovieItem("m5", "파묘", "포스터 5", 134, listOf("11:00", "14:10", "17:20")),
        MovieItem("m6", "스파이더맨", "포스터 6", 140, listOf("13:30", "16:20", "19:10")),
        MovieItem("m7", "엘리멘탈", "포스터 7", 109, listOf("09:30", "11:50", "14:10")),
        MovieItem("m8", "탑건: 매버릭", "포스터 8", 130, listOf("12:40", "15:30", "18:20")),
        MovieItem("m9", "오펜하이머", "포스터 9", 180, listOf("10:10", "13:50", "17:30"))
    )
}

@Composable
fun rememberTheaters(): List<TheaterOption> = remember {
    listOf(
        TheaterOption("t1", "1관 2D",   120, 80), // 40석 점유
        TheaterOption("t2", "2관 4DX",   96, 86), // 10석 점유
        TheaterOption("t3", "3관 IMAX",  84, 33)  // 51석 점유
    )
}

// ✅ 랜덤 좌석 점유 로직 (기존 유지)
@Composable
fun rememberReservedSeats(theater: TheaterOption?): Set<String> = remember(theater) {
    if (theater == null) return@remember emptySet()

    val allSeats = mutableListOf<String>()
    when (theater.id) {
        "t1" -> {
            for (row in 'A'..'J') { for (col in 1..12) { allSeats.add("$row$col") } }
        }
        "t2" -> {
            for (row in 'A'..'H') { for (col in 1..12) { allSeats.add("$row$col") } }
        }
        "t3" -> {
            for (row in 'A'..'G') { for (col in 1..12) { allSeats.add("$row$col") } }
        }
        else -> {}
    }

    val actualTotal = allSeats.size
    val targetReservedCount = (actualTotal - theater.remainingSeats).coerceAtLeast(0)
    allSeats.shuffled().take(targetReservedCount).toSet()
}

@Composable
fun rememberBookedTickets(
    movies: List<MovieItem> = rememberMovies(),
    theaters: List<TheaterOption> = rememberTheaters()
): Map<String, BookedTicket> = remember(movies, theaters) {
    val ticketList = listOf(
        BookedTicket(
            bookingNumber = "112233445566", movieId = "m1", theaterId = "t1", time = "10:30",
            seats = listOf("C5", "C6"), adultCount = 2, childCount = 0, seniorCount = 0
        ),
        BookedTicket(
            bookingNumber = "998877665544", movieId = "m2", theaterId = "t2", time = "12:20",
            seats = listOf("J1", "J2", "J3"), adultCount = 1, childCount = 2, seniorCount = 0
        ),
        BookedTicket(
            bookingNumber = "123456789012", movieId = "m3", theaterId = "t3", time = "11:10",
            seats = listOf("A1"), adultCount = 1, childCount = 0, seniorCount = 0
        )
    )
    ticketList.associateBy { it.bookingNumber }
}

/**
 * ✅ [수정] 미션 텍스트에 '상영관 이름'을 명확하게 표기
 */
@Composable
fun rememberCinemaMissions(): List<RequiredTicketMission> = remember {
    listOf(
        RequiredTicketMission(
            id = 1,
            // 문장형: "언제, 어디서, 무엇을, 어떻게"
            title = "10:30에 상영하는 '인사이드 아웃 2'를 1관(2D)에서 성인 2명으로 카드를 이용해 결제하세요.",
            requiredMovieId = "m1",
            requiredTime = "10:30",
            requiredTheaterId = "t1",
            requiredAdult = 2,
            requiredChild = 0,
            requiredSenior = 0,
            requiredPaymentMethod = "CARD",
            requiredFood = emptyList()
        ),
        RequiredTicketMission(
            id = 2,
            title = "12:20에 상영하는 '범죄도시 4'를 2관(4DX)에서 성인 1명, 아이 1명으로 QR코드를 이용해 결제하세요.",
            requiredMovieId = "m2",
            requiredTime = "12:20",
            requiredTheaterId = "t2",
            requiredAdult = 1,
            requiredChild = 1,
            requiredSenior = 0,
            requiredPaymentMethod = "QR",
            requiredFood = emptyList()
        ),
        RequiredTicketMission(
            id = 3,
            title = "15:00에 상영하는 '웡카'를 3관(IMAX)에서 성인 3명으로 카드를 이용해 결제하세요.",
            requiredMovieId = "m4",
            requiredTime = "15:00",
            requiredTheaterId = "t3",
            requiredAdult = 3,
            requiredChild = 0,
            requiredSenior = 0,
            requiredPaymentMethod = "CARD",
            requiredFood = emptyList()
        ),
        RequiredTicketMission(
            id = 4,
            title = "11:00에 상영하는 '파묘'를 1관(2D)에서 우대 1명으로 QR코드를 이용해 결제하세요.",
            requiredMovieId = "m5",
            requiredTime = "11:00",
            requiredTheaterId = "t1",
            requiredAdult = 0,
            requiredChild = 0,
            requiredSenior = 1,
            requiredPaymentMethod = "QR",
            requiredFood = emptyList()
        )
    )
}