package com.deskpet.app

import android.app.Application
import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.repository.PetRepository
import com.deskpet.app.data.model.InteractionLog
import com.deskpet.app.data.model.InteractionType
import com.deskpet.app.service.PetMemoryEngine
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point. Provides singletons for the Room database and
 * the [PetRepository] used across the app.
 */
class DeskPetApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: PetRepository by lazy { PetRepository.getInstance(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        val prefs = getSharedPreferences("deskpet_prefs", MODE_PRIVATE)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        SoundHelper.init()
        SoundHelper.setEnabled(soundEnabled)
        SoundHelper.play(SoundType.GREETING)

        // Log app open and generate diary if needed
        appScope.launch {
            database.interactionLogDao().insert(InteractionLog(
                type = InteractionType.OPEN_APP.name,
                timestamp = System.currentTimeMillis()
            ))
            PetMemoryEngine(database, repository).generateIfNeeded()
        }
    }

    fun logAppClose() {
        appScope.launch {
            database.interactionLogDao().insert(InteractionLog(
                type = InteractionType.CLOSE_APP.name,
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    companion object {
        @Volatile
        private var instance: DeskPetApplication? = null

        fun get(): DeskPetApplication = instance
            ?: throw IllegalStateException("DeskPetApplication.onCreate() has not been called yet")
    }
}
