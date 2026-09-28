package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {

    @Query("SELECT * FROM scripts ORDER BY updatedAt DESC")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE favorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentScripts(limit: Int = 5): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchScripts(query: String): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE id = :id LIMIT 1")
    suspend fun getScriptById(id: Long): ScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity): Long

    @Update
    suspend fun updateScript(script: ScriptEntity)

    @Query("UPDATE scripts SET lastPosition = :position, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLastPosition(id: Long, position: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE scripts SET favorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM scripts WHERE id = :id")
    suspend fun deleteScriptById(id: Long)

    @Query("SELECT COUNT(*) FROM scripts")
    suspend fun getScriptCount(): Int
}
