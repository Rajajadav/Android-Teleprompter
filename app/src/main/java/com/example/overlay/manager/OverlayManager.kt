package com.example.overlay.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.overlay.service.TeleprompterOverlayService

object OverlayManager {

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun startFloatingPrompter(context: Context, scriptId: Long) {
        val intent = Intent(context, TeleprompterOverlayService::class.java).apply {
            action = TeleprompterOverlayService.ACTION_START_OVERLAY
            putExtra(TeleprompterOverlayService.EXTRA_SCRIPT_ID, scriptId)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopFloatingPrompter(context: Context) {
        val intent = Intent(context, TeleprompterOverlayService::class.java).apply {
            action = TeleprompterOverlayService.ACTION_STOP_OVERLAY
        }
        context.startService(intent)
    }
}
