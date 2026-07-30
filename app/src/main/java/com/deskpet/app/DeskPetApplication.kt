package com.deskpet.app

import android.app.Application
import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.repository.PetRepository
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType

/**
 * Application entry point. Provides singletons for the Room database and
 * the [PetRepository] used across the app.
 */
class DeskPetApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: PetRepository by lazy { PetRepository.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val prefs = getSharedPreferences("deskpet_prefs", MODE_PRIVATE)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        SoundHelper.init()
        SoundHelper.setEnabled(soundEnabled)
        SoundHelper.play(SoundType.GREETING)
    }

    companion object {
        @Volatile
        private var instance: DeskPetApplication? = null

        fun get(): DeskPetApplication = instance
            ?: throw IllegalStateException("DeskPetApplication.onCreate() has not been called yet")
    }
}
