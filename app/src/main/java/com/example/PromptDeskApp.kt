package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.ScriptRepository

class PromptDeskApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var scriptRepository: ScriptRepository
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        scriptRepository = ScriptRepository(database.scriptDao())
        preferencesManager = PreferencesManager(this)
        authRepository = AuthRepository()
    }

    companion object {
        lateinit var instance: PromptDeskApp
            private set
    }
}
