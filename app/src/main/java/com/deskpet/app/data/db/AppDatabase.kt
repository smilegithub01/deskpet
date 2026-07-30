package com.deskpet.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deskpet.app.data.model.EnvCache
import com.deskpet.app.data.model.HabitStreak
import com.deskpet.app.data.model.InteractionLog
import com.deskpet.app.data.model.MoodLog
import com.deskpet.app.data.model.PeriodLog
import com.deskpet.app.data.model.PetDiary
import com.deskpet.app.data.model.PetEntity

@Database(
    entities = [
        MoodLog::class,
        PeriodLog::class,
        PetEntity::class,
        InteractionLog::class,
        PetDiary::class,
        HabitStreak::class,
        EnvCache::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodLogDao(): MoodLogDao
    abstract fun periodLogDao(): PeriodLogDao
    abstract fun petDao(): PetDao
    abstract fun interactionLogDao(): InteractionLogDao
    abstract fun petDiaryDao(): PetDiaryDao
    abstract fun habitStreakDao(): HabitStreakDao
    abstract fun envCacheDao(): EnvCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deskpet.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pet_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        species TEXT NOT NULL,
                        color TEXT NOT NULL,
                        level INTEGER NOT NULL,
                        hunger INTEGER NOT NULL,
                        mood INTEGER NOT NULL,
                        intimacy INTEGER NOT NULL,
                        diamonds INTEGER NOT NULL,
                        personalityTags TEXT NOT NULL,
                        equippedHead TEXT,
                        equippedGlasses TEXT,
                        equippedCollar TEXT,
                        equippedClothing TEXT,
                        equippedTail TEXT,
                        equippedAccessory TEXT,
                        createdAt INTEGER NOT NULL,
                        lastInteractionTime INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS interaction_logs (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        detail TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pet_diaries (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        content TEXT NOT NULL,
                        moodSnapshot TEXT NOT NULL,
                        petEmoji TEXT NOT NULL DEFAULT '🐱'
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pet_diaries_date ON pet_diaries(date)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS habit_streaks (
                        habitType TEXT NOT NULL PRIMARY KEY,
                        currentStreak INTEGER NOT NULL DEFAULT 0,
                        longestStreak INTEGER NOT NULL DEFAULT 0,
                        lastCheckDate TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS env_cache (
                        `key` TEXT NOT NULL PRIMARY KEY,
                        value TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
