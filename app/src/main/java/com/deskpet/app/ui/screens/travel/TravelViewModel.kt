package com.deskpet.app.ui.screens.travel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.TRAVEL_DESTINATIONS
import com.deskpet.app.data.model.TravelDestination
import com.deskpet.app.data.model.TravelLog
import com.deskpet.app.service.TravelEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TravelViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = getApplication<DeskPetApplication>().repository
    private val travelEngine = TravelEngine(
        getApplication<DeskPetApplication>().database,
        repository
    )

    val pet by lazy { repository.pet }
    val postcards by lazy { travelEngine.getAllPostcards() }

    private val _activeTravel = MutableStateFlow<TravelLog?>(null)
    val activeTravel: StateFlow<TravelLog?> = _activeTravel

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    private val _returnResult = MutableStateFlow<TravelEngine.TravelReturnResult?>(null)
    val returnResult: StateFlow<TravelEngine.TravelReturnResult?> = _returnResult

    init {
        checkTravelStatus()
    }

    fun checkTravelStatus() {
        viewModelScope.launch {
            val result = travelEngine.checkTravelReturn()
            if (result.returned) {
                _returnResult.value = result
                _toast.value = result.message
            }
            _activeTravel.value = travelEngine.getActiveTravel()
        }
    }

    fun startTravel(destination: TravelDestination, durationMs: Long) {
        viewModelScope.launch {
            val result = travelEngine.startTravel(destination.id, durationMs)
            _toast.value = result.message
            if (result.success) {
                _activeTravel.value = travelEngine.getActiveTravel()
            }
        }
    }

    fun getDestinations(): List<TravelDestination> = TRAVEL_DESTINATIONS

    fun onToastShown() { _toast.value = null }
    fun onReturnResultHandled() { _returnResult.value = null }
}
