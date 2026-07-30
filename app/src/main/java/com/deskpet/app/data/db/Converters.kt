package com.deskpet.app.data.db

import androidx.room.TypeConverter
import com.deskpet.app.data.model.MoodLevel
import com.deskpet.app.data.model.PersonalityTag
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies

/**
 * Room type converters for enum persistence.
 */
class Converters {

    @TypeConverter
    fun toMoodLevel(value: String): MoodLevel = MoodLevel.valueOf(value)

    @TypeConverter
    fun fromMoodLevel(level: MoodLevel): String = level.name

    @TypeConverter
    fun toPetSpecies(value: String): PetSpecies = PetSpecies.valueOf(value)

    @TypeConverter
    fun fromPetSpecies(species: PetSpecies): String = species.name

    @TypeConverter
    fun toPetColor(value: String): PetColor = PetColor.valueOf(value)

    @TypeConverter
    fun fromPetColor(color: PetColor): String = color.name

    @TypeConverter
    fun toPersonalityTagList(value: String): List<PersonalityTag> =
        if (value.isBlank()) emptyList()
        else value.split(",").map { PersonalityTag.valueOf(it) }

    @TypeConverter
    fun fromPersonalityTagList(tags: List<PersonalityTag>): String =
        tags.joinToString(",") { it.name }
}
