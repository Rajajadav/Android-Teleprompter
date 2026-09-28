package com.example.overlay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.PromptDeskApp
import com.example.R
import com.example.data.local.ScriptEntity
import com.example.data.repository.ScriptRepository
import com.example.overlay.view.FloatingPrompterView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TeleprompterOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: FloatingPrompterView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentScriptId: Long = 0L

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()

        // Acquire partial wake lock so screen does not dim during active teleprompter recording
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "PromptDesk:OverlayWakeLock"
            ).apply {
                acquire(2 * 60 * 60 * 1000L) // 2 hours max safe limit
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock acquire failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_OVERLAY

        when (action) {
            ACTION_START_OVERLAY -> {
                currentScriptId = intent?.getLongExtra(EXTRA_SCRIPT_ID, 0L) ?: 0L
                val notification = buildForegroundNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                showOverlay(currentScriptId)
            }
            ACTION_STOP_OVERLAY -> {
                stopOverlayAndSelf()
            }
            ACTION_TOGGLE_PLAY -> {
                floatingView?.togglePlayPause()
            }
        }

        return START_NOT_STICKY
    }

    private fun showOverlay(scriptId: Long) {
        serviceScope.launch {
            val repository: ScriptRepository = PromptDeskApp.instance.scriptRepository
            val script = if (scriptId > 0) {
                repository.getScriptById(scriptId)
            } else {
                repository.allScripts.firstOrNull()?.firstOrNull() ?: ScriptEntity(
                    title = "Quick Teleprompter",
                    content = """
                        Welcome to PromptDesk Floating Overlay!

                        Position this window near your camera lens.

                        Open your native camera app and press record.

                        Tap here to play or pause anytime!
                    """.trimIndent()
                )
            }

            if (floatingView == null) {
                floatingView = FloatingPrompterView(
                    context = this@TeleprompterOverlayService,
                    windowManager = windowManager,
                    onSavePosition = { x, y, scrollY ->
                        script?.let { s ->
                            serviceScope.launch(Dispatchers.IO) {
                                val updated = s.copy(
                                    overlayX = x,
                                    overlayY = y,
                                    lastPosition = scrollY
                                )
                                repository.updateScript(updated)
                            }
                        }
                    },
                    onCloseRequest = {
                        stopOverlayAndSelf()
                    }
                )

                try {
                    windowManager.addView(floatingView, floatingView?.windowParams)
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding overlay view: ${e.message}")
                }
            }

            script?.let { floatingView?.setScriptData(it) }
        }
    }

    private fun stopOverlayAndSelf() {
        if (floatingView != null) {
            try {
                floatingView?.onDestroy()
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay: ${e.message}")
            } finally {
                floatingView = null
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, TeleprompterOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, TeleprompterOverlayService::class.java).apply {
            action = ACTION_TOGGLE_PLAY
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            2,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("PromptDesk Teleprompter")
            .setContentText("Teleprompter overlay is active above your camera")
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_play, "Play / Pause", togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PromptDesk Teleprompter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing controls while floating teleprompter is active"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            try {
                floatingView?.onDestroy()
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay in onDestroy: ${e.message}")
            }
        }
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock release error: ${e.message}")
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "TeleprompterOverlay"
        const val CHANNEL_ID = "promptdesk_teleprompter_overlay"
        const val NOTIFICATION_ID = 4041

        const val ACTION_START_OVERLAY = "com.example.action.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"
        const val ACTION_TOGGLE_PLAY = "com.example.action.TOGGLE_PLAY"
        const val EXTRA_SCRIPT_ID = "extra_script_id"
    }
}
