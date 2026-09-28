package com.example.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean = false
)

class AuthRepository {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("AuthRepository", "FirebaseAuth not configured: ${e.message}")
            null
        }
    }

    private val _currentUserState = MutableStateFlow<UserProfile?>(null)
    val currentUserState: StateFlow<UserProfile?> = _currentUserState.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _currentUserState.value = user?.toUserProfile()
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Auth listener failed: ${e.message}")
        }
    }

    fun getCurrentUser(): UserProfile? {
        return auth?.currentUser?.toUserProfile()
    }

    fun signOut() {
        try {
            auth?.signOut()
            _currentUserState.value = null
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign out error: ${e.message}")
        }
    }

    private fun FirebaseUser.toUserProfile(): UserProfile {
        return UserProfile(
            uid = this.uid,
            email = this.email,
            displayName = this.displayName ?: this.email?.substringBefore('@') ?: "Creator",
            isAnonymous = this.isAnonymous
        )
    }
}
