package com.techlad.streakup.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techlad.streakup.data.repository.HabitRepository
import com.techlad.streakup.domain.model.HabitWithStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val habits: List<HabitWithStatus> = emptyList(),
    val completionPercent: Float = 0f,
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    val habits: StateFlow<List<HabitWithStatus>> = habitRepository
        .getActiveHabitsWithStatus()
        .mapLatest { habitRepository.enrichHabitsWithStreaks(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completionPercent: StateFlow<Float> = habitRepository
        .getTodayCompletionPercent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private val _reorderMode = MutableStateFlow(false)
    val reorderMode: StateFlow<Boolean> = _reorderMode.asStateFlow()

    fun toggleCheckIn(habitId: String) {
        viewModelScope.launch { habitRepository.toggleCheckIn(habitId) }
    }

    fun toggleReorderMode() {
        _reorderMode.value = !_reorderMode.value
    }

    fun reorderHabits(orderedIds: List<String>) {
        viewModelScope.launch { habitRepository.reorderHabits(orderedIds) }
    }
}
