package com.deskpet.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Utility for checking and requesting the permissions required by the desk pet.
 *
 * The two critical permissions are:
 *  - [Settings.canDrawOverlays] (SYSTEM_ALERT_WINDOW) — needed for the floating pet
 *  - POST_NOTIFICATIONS (Android 13+) — needed for feeding / health reminders
 */
object PermissionHelper {

    /** Whether the app is allowed to draw over other apps. */
    fun canDrawOverlays(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    /** Open the system "Display over other apps" settings page for this app. */
    fun requestOverlayPermission(context: Context) {
        if (!canDrawOverlays(context)) {
            runCatching {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
            }
        }
    }

    /** Whether the app has POST_NOTIFICATIONS permission (always true below API 33). */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
