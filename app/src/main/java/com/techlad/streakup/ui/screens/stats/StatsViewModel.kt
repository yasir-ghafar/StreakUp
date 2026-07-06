package com.techlad.streakup.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techlad.streakup.data.repository.HabitRepository
import com.techlad.streakup.domain.model.DayCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StatsPeriod { WEEKLY, MONTHLY }

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.WEEKLY,
    val weeklyData: List<DayCompletion> = emptyList(),
    val monthlyData: List<DayCompletion> = emptyList(),
    val isLoading: Boolean = true,
)

class StatsViewModel(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun setPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
    }

    private fun loadStats() {
        viewModelScope.launch {
            val weekly = habitRepository.getWeeklyStats()
            val monthly = habitRepository.getMonthlyStats()
            _uiState.value = StatsUiState(
                weeklyData = weekly,
                monthlyData = monthly,
                isLoading = false,
            )
        }
    }
}
