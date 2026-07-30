package com.deskpet.app.ui.screens.codex

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.Achievement
import com.deskpet.app.data.model.AchievementCategory
import com.deskpet.app.service.AchievementEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CodexViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = getApplication<DeskPetApplication>().repository
    private val achievementEngine = AchievementEngine(
        getApplication<DeskPetApplication>().database,
        repository
    )

    val unlockedRecords by lazy {
        achievementEngine.getUnlockedAchievements()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val allAchievements: List<Achievement> = achievementEngine.ALL_ACHIEVEMENTS

    private val _newlyUnlocked = MutableStateFlow<List<Achievement>>(emptyList())
    val newlyUnlocked: StateFlow<List<Achievement>> = _newlyUnlocked

    init {
        checkAchievements()
    }

    fun checkAchievements() {
        viewModelScope.launch {
            val result = achievementEngine.checkAll()
            if (result.newAchievements.isNotEmpty()) {
                _newlyUnlocked.value = result.newAchievements
            }
        }
    }

    fun clearNewlyUnlocked() {
        _newlyUnlocked.value = emptyList()
    }
}
