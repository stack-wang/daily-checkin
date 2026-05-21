package com.example.checkin.ui.create

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.repository.CheckInRepository
import com.example.checkin.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val repository: CheckInRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun create(project: CheckInProject) {
        viewModelScope.launch {
            val id = repository.createProject(project)
            val created = repository.getProject(id)
            if (created != null) {
                ReminderScheduler.schedule(context, created)
            }
        }
    }
}
