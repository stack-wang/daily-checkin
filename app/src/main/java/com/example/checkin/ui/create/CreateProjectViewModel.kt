package com.example.checkin.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val repository: CheckInRepository
) : ViewModel() {

    fun create(project: CheckInProject) {
        viewModelScope.launch {
            repository.createProject(project)
        }
    }
}
