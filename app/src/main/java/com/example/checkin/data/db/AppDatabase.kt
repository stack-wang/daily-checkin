package com.example.checkin.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun recordDao(): RecordDao
    abstract fun statsDao(): StatsDao
}
