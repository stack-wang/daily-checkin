package com.example.checkin.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _makeUpDays = MutableStateFlow(7)
    val makeUpDays: StateFlow<Int> = _makeUpDays.asStateFlow()

    fun setMakeUpDays(days: Int) {
        _makeUpDays.value = days.coerceIn(1, 30)
    }
}
