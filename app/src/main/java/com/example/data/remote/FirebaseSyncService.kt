package com.example.data.remote

import android.util.Log
import com.example.data.local.ScriptEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Optional cloud synchronization service.
 * Safely fails if Firebase services are unconfigured or offline.
 */
class FirebaseSyncService {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseSyncService", "Firebase Firestore unavailable: ${e.message}")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseSyncService", "FirebaseAuth unavailable: ${e.message}")
            null
        }
    }

    suspend fun syncScriptToCloud(script: ScriptEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = auth?.currentUser
        if (currentUser == null) {
            return@withContext Result.failure(IllegalStateException("User not signed in"))
        }

        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore uninitialized"))

        try {
            val scriptMap = hashMapOf(
                "title" to script.title,
                "content" to script.content,
                "wordCount" to script.wordCount,
                "fontSize" to script.fontSize,
                "scrollSpeed" to script.scrollSpeed,
                "mirrorMode" to script.mirrorMode,
                "alignment" to script.alignment,
                "updatedAt" to script.updatedAt,
                "userId" to currentUser.uid
            )

            db.collection("users")
                .document(currentUser.uid)
                .collection("scripts")
                .document(script.id.toString())
                .set(scriptMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseSyncService", "Sync error: ${e.message}")
            Result.failure(e)
        }
    }
}
