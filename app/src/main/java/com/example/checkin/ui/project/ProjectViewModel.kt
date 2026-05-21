package com.example.checkin.ui.project

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.repository.CheckInRepository
import com.example.checkin.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: CheckInRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val projects: StateFlow<List<CheckInProject>> = repository.observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(project: CheckInProject) {
        viewModelScope.launch {
            ReminderScheduler.cancel(context, project.id)
            repository.deleteProject(project)
        }
    }
}
