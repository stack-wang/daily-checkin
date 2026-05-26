package com.example.checkin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.db.entity.CheckInRecord
import com.example.checkin.data.db.entity.CheckInStats
import com.example.checkin.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val projects: List<CheckInProject> = emptyList(),
    val todayRecords: List<CheckInRecord> = emptyList(),
    val missedItems: Map<Long, List<String>> = emptyMap(),
    val makeUpDays: Int = 7,
    val projectStats: Map<Long, CheckInStats> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProjects().collect { projects ->
                loadTodayData(projects)
            }
        }
        viewModelScope.launch {
            repository.makeUpDaysFlow.collect {
                val projects = repository.getAllProjects()
                loadTodayData(projects)
            }
        }
    }

    private suspend fun loadTodayData(projects: List<CheckInProject>) {
        val today = repository.today()
        val records = repository.getRecordsByDate(today)
        val makeUpDays = repository.makeUpDaysFlow.value
        val statsList = repository.getAllStats()
        val projectStats = statsList.associateBy { it.projectId }

        val missedItems = mutableMapOf<Long, List<String>>()
        for (project in projects) {
            if (!project.makeUpEnabled) continue
            val missed = repository.getMissedCheckIns(project)
            if (missed.isNotEmpty()) {
                missedItems[project.id] = missed
            }
        }

        _uiState.value = HomeUiState(
            projects = projects,
            todayRecords = records,
            missedItems = missedItems,
            makeUpDays = makeUpDays,
            projectStats = projectStats,
            isLoading = false
        )
    }

    fun checkIn(projectId: Long) {
        viewModelScope.launch {
            repository.checkIn(projectId, repository.today())
            val projects = repository.getAllProjects()
            loadTodayData(projects)
        }
    }

    fun makeUpCheckIn(projectId: Long, date: String) {
        viewModelScope.launch {
            repository.makeUpCheckIn(projectId, date)
            val projects = repository.getAllProjects()
            loadTodayData(projects)
        }
    }

    fun uncheckIn(projectId: Long) {
        viewModelScope.launch {
            repository.uncheckIn(projectId, repository.today())
            val projects = repository.getAllProjects()
            loadTodayData(projects)
        }
    }
}
