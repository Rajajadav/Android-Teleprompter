package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ScriptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scriptDao(): ScriptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "promptdesk_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialScripts(database.scriptDao())
                    }
                }
            }

            private suspend fun populateInitialScripts(dao: ScriptDao) {
                dao.insertScript(
                    ScriptEntity(
                        title = "Welcome to PromptDesk",
                        content = """
                            Welcome to PromptDesk — Speak naturally, Create confidently!

                            This is your new professional studio teleprompter. Look straight into the camera lens while reading this text smoothly.

                            Tap anywhere on the screen while the prompter is running to pause or resume playback.

                            Double tap to reveal control panels for adjusting scroll speed, font size, mirror mode, and text width.

                            You can adjust your scrolling speed from 0.1x to 5.0x using the controls at the bottom.

                            Try turning on Voice Follow mode: PromptDesk listens to your voice and scrolls forward at your natural speaking cadence!

                            Ready to record? Switch to Camera mode to record high-definition video while delivering your message with perfect eye contact.

                            Enjoy creating amazing content!
                        """.trimIndent(),
                        wordCount = 115,
                        fontSize = 42f,
                        scrollSpeed = 1.2f,
                        favorite = true
                    )
                )

                dao.insertScript(
                    ScriptEntity(
                        title = "YouTube Video Hook & Intro",
                        content = """
                            Hey everyone, welcome back to the channel!

                            In today's video, I'm going to share the top three secrets that completely transformed how I produce content in half the time.

                            Before we dive in, make sure you hit that subscribe button and turn on the notification bell so you never miss another upload.

                            Secret number one: Scripting with intention. When you know exactly what points you want to cover, your delivery is crisp, energetic, and engaging.

                            Let's get right into point number one!
                        """.trimIndent(),
                        wordCount = 78,
                        fontSize = 44f,
                        scrollSpeed = 1.3f,
                        favorite = false
                    )
                )

                dao.insertScript(
                    ScriptEntity(
                        title = "Product Launch Pitch",
                        content = """
                            Good morning, partners and investors.

                            Today, we are thrilled to unveil our latest breakthrough. We set out with a simple question: How can creators connect deeper with their audience?

                            Our answer is simple, powerful, and accessible everywhere.

                            Here is what makes this product extraordinary: It eliminates friction. It empowers your voice. And it saves hours in every single recording session.

                            Thank you for joining us on this exciting journey.
                        """.trimIndent(),
                        wordCount = 68,
                        fontSize = 42f,
                        scrollSpeed = 1.2f,
                        favorite = true
                    )
                )
            }
        }
    }
}
