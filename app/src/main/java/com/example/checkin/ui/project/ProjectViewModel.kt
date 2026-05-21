package com.example.checkin.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: CheckInRepository
) : ViewModel() {

    val projects: StateFlow<List<CheckInProject>> = repository.observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(project: CheckInProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }
}
