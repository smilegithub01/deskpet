package com.deskpet.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.deskpet.app.MainActivity
import com.deskpet.app.R

/**
 * Helper for sending pet-themed notifications.
 *
 * All notifications use warm, pet-voice copy (e.g. "小团子饿啦~") rather than
 * cold system messages.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "pet_reminders"
    private const val CHANNEL_NAME = "宠物提醒"

    private const val NOTIF_FEEDING = 2001
    private const val NOTIF_WATER = 2002
    private const val NOTIF_PERIOD = 2003
    private const val NOTIF_MOOD = 2004

    /** Create the reminder notification channel. Call once at app start. */
    fun createPetNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "桌面宠物的喂食、健康和心情提醒"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showFeedingReminder(context: Context, petName: String = "小团子") {
        notify(context, NOTIF_FEEDING, "小团子饿啦~", "$petName 肚子咕咕叫了，快来喂喂它吧！")
    }

    fun showWaterReminder(context: Context) {
        notify(context, NOTIF_WATER, "该喝水啦~", "小团子提醒你补充水分，它也想喝一口呢～")
    }

    fun showPeriodReminder(context: Context, petName: String = "小团子", daysUntil: Int) {
        val text = if (daysUntil <= 0) {
            "$petName 感觉主人可能快要不方便了，记得准备好需要的物品哦~"
        } else if (daysUntil <= 3) {
            "$petName 感觉快要到了，记得准备哦~"
        } else {
            "预计还有 $daysUntil 天，$petName 已经准备好更粘你了~"
        }
        notify(context, NOTIF_PERIOD, "经期提醒", text)
    }

    fun showPeriodEndedMessage(context: Context, petName: String = "小团子") {
        notify(context, NOTIF_PERIOD, "经期结束", "$petName 松了一口气，主人辛苦啦~")
    }

    fun showMoodReminder(context: Context) {
        notify(context, NOTIF_MOOD, "今天心情怎么样？", "小团子想听听你今天的故事～")
    }

    // ------------------------------------------------------------------ private

    private fun notify(context: Context, id: Int, title: String, text: String) {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_pet_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }
}
