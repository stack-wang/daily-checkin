package com.example.checkin.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.checkin.data.db.dao.ProjectDao
import com.example.checkin.data.db.dao.RecordDao
import com.example.checkin.data.db.dao.StatsDao
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.db.entity.CheckInRecord
import com.example.checkin.data.db.entity.CheckInStats

@Database(
    entities = [
        CheckInProject::class,
        CheckInRecord::class,
        CheckInStats::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun recordDao(): RecordDao
    abstract fun statsDao(): StatsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE checkin_projects ADD COLUMN reward_enabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE checkin_projects ADD COLUMN makeup_enabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE checkin_stats ADD COLUMN total_reward INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
