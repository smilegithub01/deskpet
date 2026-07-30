package com.deskpet.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.deskpet.app.MainActivity
import com.deskpet.app.R
import com.deskpet.app.data.model.PetState
import com.deskpet.app.ui.components.OverlayPetContent
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground Service that displays the desk pet as a system overlay window.
 *
 * Uses [WindowManager] with [LayoutParams.TYPE_APPLICATION_OVERLAY] to float
 * the pet above all other apps — just like the Windows desktop pet experience.
 * Supports drag-to-move, idle walking, click-to-happy, and three-level toggle
 * (show / pause / hide).
 */
class PetOverlayService : Service(), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var layoutParams: LayoutParams? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _petState = MutableStateFlow(PetState.IDLE)
    val petState: StateFlow<PetState> = _petState.asStateFlow()

    private val _isVisible = MutableStateFlow(false)
    private val _isPaused = MutableStateFlow(false)
    private val _smartAvoidance = MutableStateFlow(true)

    // ------------------------------------------------------------------ lifecycle

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        createNotificationChannel()
        startForegroundCompat()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
            ACTION_PAUSE -> setPaused(true)
            ACTION_RESUME -> setPaused(false)
            ACTION_TOGGLE_AVOIDANCE -> _smartAvoidance.value = !_smartAvoidance.value
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        runCatching {
            overlayView?.let { windowManager.removeView(it) }
        }
        overlayView = null
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }

    // ------------------------------------------------------------------ overlay window

    private fun showOverlay() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(this)) return

        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") LayoutParams.TYPE_PHONE,
            LayoutParams.FLAG_NOT_FOCUSABLE or
                LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 48
            y = 120
        }

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
            setContent {
                OverlayPetContent(
                    petState = _petState.collectAsState().value,
                    isVisible = _isVisible.collectAsState().value,
                    isPaused = _isPaused.collectAsState().value,
                    onDrag = { dx, dy -> moveBy(dx, dy) },
                    onClick = { onPetClicked() },
                    onLongPress = { showQuickMenu() }
                )
            }
        }

        // Attach a LifecycleOwner so ComposeView can observe lifecycle
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        windowManager.addView(composeView, params)
        overlayView = composeView
        layoutParams = params
        _isVisible.value = true
        startBehaviorLoop()
    }

    private fun moveBy(dx: Float, dy: Float) {
        val params = layoutParams ?: return
        params.x += dx.toInt()
        params.y -= dy.toInt() // y is inverted in Android window coordinates

        // Smart avoidance: push pet away from screen center
        if (_smartAvoidance.value) {
            val dm = resources.displayMetrics
            val centerX = dm.widthPixels / 2
            if (kotlin.math.abs(params.x - centerX) < 200) {
                params.x = if (params.x < centerX) 48 else dm.widthPixels - 200
            }
        }

        runCatching { windowManager.updateViewLayout(overlayView, params) }
    }

    private fun onPetClicked() {
        if (_isPaused.value) {
            setPaused(false)
            return
        }
        SoundHelper.play(SoundType.PET)
        _petState.value = PetState.HAPPY
        scope.launch {
            delay(3000)
            _petState.value = PetState.IDLE
        }
    }

    private fun showQuickMenu() {
        // For now, toggle pause as a quick action.
        // In production: show a PopupWindow with menu items (pause / hide / open app / settings).
        setPaused(!_isPaused.value)
    }

    private fun setPaused(paused: Boolean) {
        _isPaused.value = paused
        _petState.value = if (paused) PetState.PAUSED else PetState.IDLE
        updateNotification(if (paused) "小团子已暂停互动" else getString(R.string.overlay_notification_title))
    }

    private fun hideOverlay() {
        runCatching {
            overlayView?.let { windowManager.removeView(it) }
        }
        overlayView = null
        layoutParams = null
        _isVisible.value = false
        _petState.value = PetState.HIDDEN
        stopSelf()
    }

    // ------------------------------------------------------------------ behavior loop

    private fun startBehaviorLoop() {
        scope.launch {
            while (_isVisible.value) {
                delay(8000 + (Math.random() * 4000).toLong()) // 8-12 s
                if (!_isPaused.value && _petState.value == PetState.IDLE) {
                    randomWalk()
                }
            }
        }
    }

    private fun randomWalk() {
        val params = layoutParams ?: return
        val dm: DisplayMetrics = resources.displayMetrics
        val maxX = dm.widthPixels - 200
        val maxY = dm.heightPixels - 400
        params.x = (Math.random() * maxX).toInt().coerceIn(0, maxX)
        params.y = (Math.random() * maxY).toInt().coerceIn(0, maxY)
        runCatching { windowManager.updateViewLayout(overlayView, params) }
    }

    // ------------------------------------------------------------------ notification

    private fun startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(getString(R.string.overlay_notification_title)),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(getString(R.string.overlay_notification_title))
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.overlay_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_pet_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    // ------------------------------------------------------------------ companion

    companion object {
        private const val CHANNEL_ID = "pet_overlay_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_SHOW = "com.deskpet.app.SHOW"
        const val ACTION_HIDE = "com.deskpet.app.HIDE"
        const val ACTION_PAUSE = "com.deskpet.app.PAUSE"
        const val ACTION_RESUME = "com.deskpet.app.RESUME"
        const val ACTION_TOGGLE_AVOIDANCE = "com.deskpet.app.TOGGLE_AVOIDANCE"

        fun start(context: Context) {
            val intent = Intent(context, PetOverlayService::class.java).apply {
                action = ACTION_SHOW
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PetOverlayService::class.java).apply {
                action = ACTION_HIDE
            }
            context.startService(intent)
        }
    }
}
