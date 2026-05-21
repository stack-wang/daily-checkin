package com.example.checkin.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkin.data.db.entity.CheckInStats
import com.example.checkin.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatsUiState(
    val stats: List<CheckInStats> = emptyList(),
    val projectNames: Map<Long, String> = emptyMap(),
    val projectColors: Map<Long, String> = emptyMap(),
    val dailyCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: CheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProjects().collect { loadStats() }
        }
        viewModelScope.launch {
            repository.observeStats().collect { loadStats() }
        }
    }

    private suspend fun loadStats() {
        val stats = repository.getAllStats()
        val projects = repository.getAllProjects()
        val projectNames = projects.associate { it.id to it.name }
        val projectColors = projects.associate { it.id to it.color }

        val today = LocalDate.now()
        val startDate = today.minusMonths(3).withDayOfMonth(1)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dailyData = repository.getDailyCountInRange(
            startDate.format(formatter),
            today.format(formatter)
        )
        val dailyCounts = dailyData.associate { it.date to it.count }

        _uiState.value = StatsUiState(
            stats = stats,
            projectNames = projectNames,
            projectColors = projectColors,
            dailyCounts = dailyCounts,
            isLoading = false
        )
    }
}
