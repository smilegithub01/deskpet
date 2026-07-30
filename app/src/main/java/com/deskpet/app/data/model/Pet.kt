package com.deskpet.app.data.model

/**
 * Main in-memory representation of the desk pet.
 *
 * Note: this is the runtime state object (kept in [PetRepository]); the
 * persistent history is stored via [MoodLog] / [PeriodLog] Room entities.
 */
data class Pet(
    val id: Long = 1,
    val name: String = "小团子",
    val species: PetSpecies = PetSpecies.CAT,
    val color: PetColor = PetColor.PINK,
    val level: Int = 12,
    val hunger: Int = 78,
    val mood: Int = 92,
    val intimacy: Int = 65,
    val diamonds: Int = 2340,
    val personalityTags: List<PersonalityTag> = listOf(
        PersonalityTag.LIVELY,
        PersonalityTag.CLINGY
    ),
    val equippedHead: String? = null,
    val equippedGlasses: String? = null,
    val equippedCollar: String? = null,
    val equippedClothing: String? = null,
    val equippedTail: String? = null,
    val equippedAccessory: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
