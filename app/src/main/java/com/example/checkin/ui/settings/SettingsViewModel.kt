package com.example.checkin.ui.settings

import androidx.lifecycle.ViewModel
import com.example.checkin.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CheckInRepository
) : ViewModel() {

    val makeUpDays: StateFlow<Int> = repository.makeUpDaysFlow

    fun setMakeUpDays(days: Int) {
        repository.makeUpDaysFlow.value = days.coerceIn(1, 30)
    }
}
