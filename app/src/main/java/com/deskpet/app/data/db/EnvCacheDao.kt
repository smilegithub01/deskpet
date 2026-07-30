package com.deskpet.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deskpet.app.data.model.EnvCache

@Dao
interface EnvCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: EnvCache)

    @Query("SELECT * FROM env_cache WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): EnvCache?

    @Query("DELETE FROM env_cache WHERE `key` = :key")
    suspend fun delete(key: String)
}
