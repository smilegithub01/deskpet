package com.deskpet.app.ui.screens.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.PetDiary
import com.deskpet.app.service.PetMemoryEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val memoryEngine = PetMemoryEngine(
        getApplication<DeskPetApplication>().database,
        getApplication<DeskPetApplication>().repository
    )

    val diaries: StateFlow<List<PetDiary>> = memoryEngine.getRecentDiaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
