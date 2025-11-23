package com.example.kiosk.ui.viewmodel // 패키지 확인

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiosk.data.model.HistoryRecord
import com.example.kiosk.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository: HistoryRepository

    // 1. 데이터를 담을 그릇 (수정 가능: Mutable)
    // 👉 여기가 _history를 선언하는 곳입니다!
    private val _history = MutableStateFlow<List<HistoryRecord>>(emptyList())

    // 2. UI가 바라볼 그릇 (읽기 전용: StateFlow)
    // 👉 UI는 이걸 구독합니다.
    val history: StateFlow<List<HistoryRecord>> = _history.asStateFlow()

    init {
        historyRepository = HistoryRepository(application)
        // 3. 앱 켜질 때 자동으로 한 번 가져오기
        fetchHistory()
    }

    // 4. 데이터를 새로고침하는 함수
    fun fetchHistory() {
        viewModelScope.launch {
            // 저장소에서 데이터를 가져와서
            val data = historyRepository.getAllHistory()
            // _history 그릇에 담아줍니다 (화면이 자동 갱신됨)
            _history.value = data
        }
    }
}