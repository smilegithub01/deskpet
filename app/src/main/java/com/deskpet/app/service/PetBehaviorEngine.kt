package com.deskpet.app.service

import com.deskpet.app.data.model.PetState
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Autonomous behavior state machine for the desk pet.
 *
 * Periodically evaluates time-of-day, hunger level, and random chance to
 * transition the pet between idle / sleepy / playing states.  Interaction-
 * triggered states (HAPPY, COMFORTING, EATING) are set explicitly and
 * auto-revert to IDLE after a short delay.
 */
class PetBehaviorEngine(
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(PetState.IDLE)
    val state: StateFlow<PetState> = _state.asStateFlow()

    private var loopJob: Job? = null

    /** Start the periodic evaluation loop. */
    fun start() {
        loopJob?.cancel()
        loopJob = scope.launch {
            while (true) {
                delay(5_000)
                evaluateState()
            }
        }
    }

    /** Stop the loop — call when the overlay is hidden. */
    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    private suspend fun evaluateState() {
        // Don't override interaction-triggered states
        when (_state.value) {
            PetState.HAPPY, PetState.COMFORTING,
            PetState.EATING, PetState.PAUSED,
            PetState.HIDDEN -> return
            else -> { /* continue */ }
        }

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        when {
            hour in 22..23 || hour in 0..7 -> {
                if (_state.value != PetState.SLEEPY) {
                    _state.value = PetState.SLEEPY
                    SoundHelper.play(SoundType.SLEEP)
                }
            }
            _state.value == PetState.IDLE && Math.random() < 0.15 -> {
                _state.value = PetState.PLAYING
                delay(10_000)
                _state.value = PetState.IDLE
            }
            _state.value == PetState.SLEEPY && hour !in 22..23 && hour !in 0..7 -> {
                _state.value = PetState.IDLE
                SoundHelper.play(SoundType.WAKE)
            }
        }
    }

    // ------------------------------------------------------------------ triggers

    /** Triggered when user pets / clicks the pet. */
    fun triggerHappy() {
        _state.value = PetState.HAPPY
        scope.launch {
            delay(3_000)
            if (_state.value == PetState.HAPPY) _state.value = PetState.IDLE
        }
    }

    /** Triggered when user logs a negative mood. */
    fun triggerComfort() {
        _state.value = PetState.COMFORTING
        scope.launch {
            delay(5_000)
            if (_state.value == PetState.COMFORTING) _state.value = PetState.IDLE
        }
    }

    /** Triggered when user feeds the pet. */
    fun triggerEating() {
        _state.value = PetState.EATING
        scope.launch {
            delay(4_000)
            if (_state.value == PetState.EATING) _state.value = PetState.IDLE
        }
    }
}
