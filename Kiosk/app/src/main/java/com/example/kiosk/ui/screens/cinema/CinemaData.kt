// app/src/main/java/com/example/kiosk/ui/screens/cinema/CinemaData.kt
package com.example.kiosk.ui.screens.cinema

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// ------------------------------------------------------------
// 스텝 정의
// ------------------------------------------------------------

// 영화 예매 메인 단계
enum class CinemaStage { HOME, BOOKING, SEAT, PAYMENT, SNACK, PRINT }

// 예매 세부 단계
enum class BookingStep { MOVIE, TIME, THEATER_PEOPLE }

// (공통) 결제 세부 단계
enum class PaymentStep { METHOD_SELECT, CARD_INSERT, QR_SCAN, PROCESSING, SUCCESS }

// 음식 주문 메인 단계
enum class FoodStep { MENU, PAYMENT }

// ------------------------------------------------------------
// 데이터 모델
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

// ------------------------------------------------------------
// 더미 데이터
// ------------------------------------------------------------

/**
 * 영화 목록 (9개)
 */
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

/**
 * 상영관 목록
 */
@Composable
fun rememberTheaters(): List<TheaterOption> = remember {
    listOf(
        TheaterOption("t1", "1관 2D",   120, 80),
        TheaterOption("t2", "2관 4DX",   96, 86),
        TheaterOption("t3", "3관 IMAX",  84, 33)
    )
}

/**
 * 상영관 ID별로 미리 예매된 좌석 Set을 생성합니다.
 */
@Composable
fun rememberReservedSeats(theaterId: String?): Set<String> = remember(theaterId) {
    when (theaterId) {
        // "1관 2D" (40석 예매됨)
        "t1" -> {
            // A, B, C열 전체 (12 * 3 = 36) + D열 4개
            val seats = mutableSetOf<String>()
            for (row in 'A'..'C') {
                for (col in 1..12) {
                    seats.add("$row$col")
                }
            }
            for (col in 1..4) { seats.add("D$col") } // 40개
            seats
        }
        // "2관 4DX" (10석 예매됨)
        "t2" -> {
            // J열 1~10번
            (1..10).map { "J$it" }.toSet() // 10개
        }
        // "3관 IMAX" (51석 예매됨)
        "t3" -> {
            // A, B, C, D열 전체 (12 * 4 = 48) + E열 3개
            val seats = mutableSetOf<String>()
            for (row in 'A'..'D') {
                for (col in 1..12) {
                    seats.add("$row$col")
                }
            }
            for (col in 1..3) { seats.add("E$col") } // 51개
            seats
        }
        // 그 외
        else -> emptySet()
    }
}


// ------------------------------------------------------------
// ✅ [요청] 예매 티켓 출력용 더미 데이터 (신규 추가)
// ------------------------------------------------------------

/**
 * 예매된 티켓 정보 모델
 */
data class BookedTicket(
    val bookingNumber: String, // 예매 번호 (12자리)
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

/**
 * 미리 예매된 티켓 목록 (3개)
 * (rememberReservedSeats 에 정의된 좌석을 기반으로 함)
 */
@Composable
fun rememberBookedTickets(
    movies: List<MovieItem> = rememberMovies(),
    theaters: List<TheaterOption> = rememberTheaters()
): Map<String, BookedTicket> = remember(movies, theaters) {
    val ticketList = listOf(
        // 티켓 1: 1관 (t1), C5, C6 (2명)
        BookedTicket(
            bookingNumber = "112233445566",
            movieId = "m1", // 인사이드 아웃 2
            theaterId = "t1", // 1관 2D
            time = "10:30",
            seats = listOf("C5", "C6"),
            adultCount = 2,
            childCount = 0,
            seniorCount = 0
        ),
        // 티켓 2: 2관 (t2), J1, J2, J3 (3명)
        BookedTicket(
            bookingNumber = "998877665544",
            movieId = "m2", // 범죄도시 4
            theaterId = "t2", // 2관 4DX
            time = "12:20",
            seats = listOf("J1", "J2", "J3"),
            adultCount = 1,
            childCount = 2,
            seniorCount = 0
        ),
        // 티켓 3: 3관 (t3), A1 (1명)
        BookedTicket(
            bookingNumber = "123456789012",
            movieId = "m3", // 듄: 파트2
            theaterId = "t3", // 3관 IMAX
            time = "11:10",
            seats = listOf("A1"),
            adultCount = 1,
            childCount = 0,
            seniorCount = 0
        )
    )
    // 조회를 쉽게 하기 위해 Map<String, BookedTicket> 형태로 변환
    ticketList.associateBy { it.bookingNumber }
}