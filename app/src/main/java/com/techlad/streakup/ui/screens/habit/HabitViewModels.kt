package com.techlad.streakup.ui.screens.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techlad.streakup.data.repository.HabitRepository
import com.techlad.streakup.domain.StreakCalculator
import com.techlad.streakup.domain.model.FrequencyType
import com.techlad.streakup.domain.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

data class HabitFormUiState(
    val name: String = "",
    val icon: String = "✅",
    val color: String = "#5DCAA5",
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyTarget: Int = 3,
    val reminderTime: LocalTime? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val editingHabitId: String? = null,
)

class HabitFormViewModel(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitFormUiState())
    val uiState: StateFlow<HabitFormUiState> = _uiState.asStateFlow()

    fun loadHabit(habitId: String) {
        viewModelScope.launch {
            val habit = habitRepository.getHabitById(habitId) ?: return@launch
            _uiState.value = HabitFormUiState(
                name = habit.name,
                icon = habit.icon,
                color = habit.color,
                frequencyType = habit.frequencyType,
                frequencyTarget = habit.frequencyTarget,
                reminderTime = habit.reminderTime,
                editingHabitId = habit.id,
            )
        }
    }

    fun updateName(name: String) { _uiState.value = _uiState.value.copy(name = name) }
    fun updateIcon(icon: String) { _uiState.value = _uiState.value.copy(icon = icon) }
    fun updateColor(color: String) { _uiState.value = _uiState.value.copy(color = color) }
    fun updateFrequencyType(type: FrequencyType) { _uiState.value = _uiState.value.copy(frequencyType = type) }
    fun updateFrequencyTarget(target: Int) { _uiState.value = _uiState.value.copy(frequencyTarget = target) }
    fun updateReminderTime(time: LocalTime?) { _uiState.value = _uiState.value.copy(reminderTime = time) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Name is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val habit = if (state.editingHabitId != null) {
                habitRepository.getHabitById(state.editingHabitId)?.copy(
                    name = state.name,
                    icon = state.icon,
                    color = state.color,
                    frequencyType = state.frequencyType,
                    frequencyTarget = state.frequencyTarget,
                    reminderTime = state.reminderTime,
                ) ?: Habit(name = state.name)
            } else {
                Habit(
                    name = state.name,
                    icon = state.icon,
                    color = state.color,
                    frequencyType = state.frequencyType,
                    frequencyTarget = state.frequencyTarget,
                    reminderTime = state.reminderTime,
                )
            }
            habitRepository.saveHabit(habit)
            _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
        }
    }
}

data class HabitDetailUiState(
    val habit: Habit? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val heatmapData: Map<java.time.LocalDate, Int> = emptyMap(),
    val weeklyProgress: Int = 0,
    val isLoading: Boolean = true,
)

class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val habitId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    init {
        habitId?.let { load(it) }
    }

    fun load(id: String) {
        viewModelScope.launch {
            val habit = habitRepository.getHabitById(id) ?: return@launch
            val dates = habitRepository.getCheckInDates(id, 365)
            val today = java.time.LocalDate.now()
            val (current, longest) = StreakCalculator.calculateStreaks(habit, dates, today)
            val heatmap = StreakCalculator.buildHeatmapData(dates, today.minusDays(90), today)
            _uiState.value = HabitDetailUiState(
                habit = habit,
                currentStreak = current,
                longestStreak = longest,
                heatmapData = heatmap,
                weeklyProgress = StreakCalculator.weeklyProgress(dates, today),
                isLoading = false,
            )
        }
    }

    fun toggleToday() {
        val id = habitId ?: return
        viewModelScope.launch {
            habitRepository.toggleCheckIn(id)
            load(id)
        }
    }

    fun archive() {
        val id = habitId ?: return
        viewModelScope.launch { habitRepository.archiveHabit(id) }
    }

    fun delete() {
        val id = habitId ?: return
        viewModelScope.launch { habitRepository.deleteHabit(id) }
    }
}
