package com.example.checkin.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.checkin.data.db.entity.CheckInStats
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Query("SELECT * FROM checkin_stats WHERE project_id = :projectId")
    suspend fun getByProject(projectId: Long): CheckInStats?

    @Query("SELECT * FROM checkin_stats ORDER BY total_count DESC")
    fun observeAll(): Flow<List<CheckInStats>>

    @Query("SELECT * FROM checkin_stats ORDER BY total_count DESC")
    suspend fun getAll(): List<CheckInStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: CheckInStats)

    @Query("DELETE FROM checkin_stats WHERE project_id = :projectId")
    suspend fun deleteByProject(projectId: Long)
}
