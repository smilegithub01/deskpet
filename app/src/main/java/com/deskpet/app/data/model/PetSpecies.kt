package com.deskpet.app.data.model

/**
 * Species of the desk pet.
 */
enum class PetSpecies(val displayName: String, val emoji: String) {
    CAT("猫咪", "🐱"),
    DOG("小狗", "🐶"),
    RABBIT("兔子", "🐰"),
    HAMSTER("仓鼠", "🐹")
}
