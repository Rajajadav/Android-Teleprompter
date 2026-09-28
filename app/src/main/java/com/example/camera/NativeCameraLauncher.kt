package com.example.camera

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log

object NativeCameraLauncher {

    private const val TAG = "NativeCameraLauncher"

    /**
     * Attempts to launch the native camera app for recording/shooting.
     * Tries video camera intent first, then standard camera intent, then package queries.
     */
    fun launchCamera(context: Context): Boolean {
        // Priority 1: Video recording mode
        val videoCameraIntent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (canResolve(context, videoCameraIntent)) {
            try {
                context.startActivity(videoCameraIntent)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to launch video camera intent: ${e.message}")
            }
        }

        // Priority 2: Standard still image camera (opens full camera app with toggle to video)
        val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (canResolve(context, cameraIntent)) {
            try {
                context.startActivity(cameraIntent)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to launch still camera intent: ${e.message}")
            }
        }

        // Priority 3: Fallback ACTION_IMAGE_CAPTURE
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (canResolve(context, captureIntent)) {
            try {
                context.startActivity(captureIntent)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to launch capture intent: ${e.message}")
            }
        }

        // Priority 4: Query known OEM camera packages (Samsung, Google, Xiaomi, OnePlus, Oppo, Vivo, Motorola)
        val knownPackages = listOf(
            "com.google.android.GoogleCamera",
            "com.sec.android.app.camera",
            "com.android.camera",
            "com.oneplus.camera",
            "com.oppo.camera",
            "com.motorola.camera2",
            "com.vivo.camera"
        )
        val packageManager = context.packageManager
        for (pkg in knownPackages) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                try {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                } catch (e: Exception) {
                    Log.w(TAG, "Failed launching pkg $pkg: ${e.message}")
                }
            }
        }

        return false
    }

    private fun canResolve(context: Context, intent: Intent): Boolean {
        val matches = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return matches.isNotEmpty()
    }
}
