package com.deskpet.app.data.model

/**
 * Behavior state machine for the pet overlay.
 */
enum class PetState {
    IDLE,
    HAPPY,
    EATING,
    HUNGRY,
    SLEEPY,
    EXCITED,
    COMFORTING,
    PLAYING,
    HIDDEN,
    PAUSED
}
