package com.deskpet.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deskpet.app.data.model.MoodLog
import com.deskpet.app.data.model.PeriodLog
import com.deskpet.app.data.model.PetEntity

@Database(
    entities = [MoodLog::class, PeriodLog::class, PetEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodLogDao(): MoodLogDao
    abstract fun periodLogDao(): PeriodLogDao
    abstract fun petDao(): PetDao

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
                    .addMigrations(MIGRATION_1_2)
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
    }
}
