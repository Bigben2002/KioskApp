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
    val title: String, // 미션 제목
    val requiredMovieId: String,
    val requiredTime: String,
    val requiredTheaterId: String,
    val requiredAdult: Int,
    val requiredChild: Int,
    val requiredSenior: Int,
    val requiredFood: List<RequiredItem> // 음식 미션 (모두 제거됨)
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
        TheaterOption("t1", "1관 2D",   120, 80),
        TheaterOption("t2", "2관 4DX",   96, 86),
        TheaterOption("t3", "3관 IMAX",  84, 33)
    )
}

@Composable
fun rememberReservedSeats(theaterId: String?): Set<String> = remember(theaterId) {
    when (theaterId) {
        "t1" -> {
            val seats = mutableSetOf<String>()
            for (row in 'A'..'C') {
                for (col in 1..12) {
                    seats.add("$row$col")
                }
            }
            for (col in 1..4) { seats.add("D$col") }
            seats
        }
        "t2" -> {
            (1..10).map { "J$it" }.toSet()
        }
        "t3" -> {
            val seats = mutableSetOf<String>()
            for (row in 'A'..'D') {
                for (col in 1..12) {
                    seats.add("$row$col")
                }
            }
            for (col in 1..3) { seats.add("E$col") }
            seats
        }
        else -> emptySet()
    }
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
 * ✅ 영화관 실전 모드 미션 목록 (음식 미션 요구사항 제거)
 */
@Composable
fun rememberCinemaMissions(): List<RequiredTicketMission> = remember {
    listOf(
        // 미션 1: 티켓만 성공 (인사이드 아웃 2, 성인 2명, 1관)
        RequiredTicketMission(
            id = 1,
            title = "오늘 10:30, 1관에서 '인사이드 아웃 2' 성인 2명의 티켓을 예매하세요.",
            requiredMovieId = "m1",
            requiredTime = "10:30",
            requiredTheaterId = "t1",
            requiredAdult = 2,
            requiredChild = 0,
            requiredSenior = 0,
            requiredFood = emptyList() // ⬅️ 음식 요구사항 제거
        ),
        // 미션 2: 티켓만 성공 (범죄도시 4, 성인 1명, 아이 1명, 2관)
        RequiredTicketMission(
            id = 2,
            title = "12:20, 2관 4DX에서 '범죄도시 4' 성인 1명, 아이 1명의 티켓을 예매하세요.",
            requiredMovieId = "m2",
            requiredTime = "12:20",
            requiredTheaterId = "t2",
            requiredAdult = 1,
            requiredChild = 1,
            requiredSenior = 0,
            requiredFood = emptyList() // ⬅️ 음식 요구사항 제거
        ),
        // 미션 3: 티켓만 성공 (웡카, 성인 3명, 3관)
        RequiredTicketMission(
            id = 3,
            title = "15:00, 3관 IMAX에서 '웡카' 성인 3명의 티켓을 예매하세요.",
            requiredMovieId = "m4",
            requiredTime = "15:00",
            requiredTheaterId = "t3",
            requiredAdult = 3,
            requiredChild = 0,
            requiredSenior = 0,
            requiredFood = emptyList() // ⬅️ 음식 요구사항 제거
        ),
        // 미션 4: 우대 포함 (파묘, 우대 1명, 1관)
        RequiredTicketMission(
            id = 4,
            title = "11:00, 1관 2D에서 '파묘' 우대 1명의 티켓을 예매하세요.",
            requiredMovieId = "m5",
            requiredTime = "11:00",
            requiredTheaterId = "t1",
            requiredAdult = 0,
            requiredChild = 0,
            requiredSenior = 1,
            requiredFood = emptyList() // ⬅️ 음식 요구사항 제거
        )
    )
}