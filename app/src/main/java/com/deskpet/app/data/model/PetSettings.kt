package com.deskpet.app.data.model

/**
 * User-configurable settings (persisted via SharedPreferences in [PetRepository]).
 */
data class PetSettings(
    val overlayEnabled: Boolean = true,
    val autoBehavior: Boolean = true,
    val smartAvoidance: Boolean = true,
    val widgetEnabled: Boolean = true,
    val liveWallpaperEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val themeColor: PetColor = PetColor.PINK,
    val periodTrackingEnabled: Boolean = true,
    val waterReminderEnabled: Boolean = true,
    val waterReminderInterval: Int = 60, // minutes
    val sitReminderEnabled: Boolean = true,
    val sitReminderInterval: Int = 120,
    val eyeReminderEnabled: Boolean = true,
    val eyeReminderInterval: Int = 45,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: Int = 23, // hour
    val quietHoursEnd: Int = 7,
    val dataEncrypted: Boolean = true
)
