package com.deskpet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.deskpet.app.data.db.Converters

@Entity(tableName = "pet_state")
@TypeConverters(Converters::class)
data class PetEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "小团子",
    val species: PetSpecies = PetSpecies.CAT,
    val color: PetColor = PetColor.PINK,
    val level: Int = 1,
    val hunger: Int = 80,
    val mood: Int = 80,
    val intimacy: Int = 50,
    val diamonds: Int = 500,
    val personalityTags: List<PersonalityTag> = listOf(PersonalityTag.LIVELY, PersonalityTag.CLINGY),
    val equippedHead: String? = null,
    val equippedGlasses: String? = null,
    val equippedCollar: String? = null,
    val equippedClothing: String? = null,
    val equippedTail: String? = null,
    val equippedAccessory: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteractionTime: Long = System.currentTimeMillis()
)

fun PetEntity.toPet(): Pet = Pet(
    id = id,
    name = name,
    species = species,
    color = color,
    level = level,
    hunger = hunger,
    mood = mood,
    intimacy = intimacy,
    diamonds = diamonds,
    personalityTags = personalityTags,
    equippedHead = equippedHead,
    equippedGlasses = equippedGlasses,
    equippedCollar = equippedCollar,
    equippedClothing = equippedClothing,
    equippedTail = equippedTail,
    equippedAccessory = equippedAccessory,
    createdAt = createdAt
)

fun Pet.toEntity(lastInteractionTime: Long = System.currentTimeMillis()): PetEntity = PetEntity(
    id = id,
    name = name,
    species = species,
    color = color,
    level = level,
    hunger = hunger,
    mood = mood,
    intimacy = intimacy,
    diamonds = diamonds,
    personalityTags = personalityTags,
    equippedHead = equippedHead,
    equippedGlasses = equippedGlasses,
    equippedCollar = equippedCollar,
    equippedClothing = equippedClothing,
    equippedTail = equippedTail,
    equippedAccessory = equippedAccessory,
    createdAt = createdAt,
    lastInteractionTime = lastInteractionTime
)
