package com.deskpet.app.ui.screens.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.HabitStreak
import com.deskpet.app.data.model.HabitType
import com.deskpet.app.data.repository.HabitCheckinResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = getApplication<DeskPetApplication>().repository
    private val habitStreakDao = getApplication<DeskPetApplication>().database.habitStreakDao()

    val habitStreaks: StateFlow<List<HabitStreak>> = habitStreakDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _checkinResult = MutableStateFlow<HabitCheckinResult?>(null)
    val checkinResult: StateFlow<HabitCheckinResult?> = _checkinResult

    fun checkin(habitType: HabitType) {
        viewModelScope.launch {
            val result = repository.checkinHabit(habitType)
            _checkinResult.value = result
        }
    }

    fun clearResult() {
        _checkinResult.value = null
    }

    fun getStreakForType(type: HabitType): HabitStreak? {
        return habitStreaks.value.find { it.habitType == type.name }
    }
}
