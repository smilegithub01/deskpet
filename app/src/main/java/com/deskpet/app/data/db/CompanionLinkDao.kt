package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.CompanionLink
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: CompanionLink)

    @Query("SELECT * FROM companion_links WHERE isActive = 1 LIMIT 1")
    fun getActiveLink(): Flow<CompanionLink?>

    @Query("SELECT * FROM companion_links ORDER BY lastUpdate DESC")
    fun getAll(): Flow<List<CompanionLink>>

    @Query("UPDATE companion_links SET isActive = 0 WHERE pairCode = :code")
    suspend fun deactivate(code: String)

    @Query("DELETE FROM companion_links WHERE pairCode = :code")
    suspend fun delete(code: String)
}
