package com.example.checkin.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.checkin.data.db.entity.CheckInProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM checkin_projects ORDER BY sort_order ASC, created_at ASC")
    fun observeAll(): Flow<List<CheckInProject>>

    @Query("SELECT * FROM checkin_projects ORDER BY sort_order ASC, created_at ASC")
    suspend fun getAll(): List<CheckInProject>

    @Query("SELECT * FROM checkin_projects WHERE id = :id")
    suspend fun getById(id: Long): CheckInProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: CheckInProject): Long

    @Update
    suspend fun update(project: CheckInProject)

    @Delete
    suspend fun delete(project: CheckInProject)

    @Query("SELECT COALESCE(MAX(sort_order), 0) FROM checkin_projects")
    suspend fun getMaxSortOrder(): Int

    @Query("UPDATE checkin_projects SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("SELECT * FROM checkin_projects WHERE reminder_enabled = 1 AND reminder_time != ''")
    suspend fun getProjectsWithReminder(): List<CheckInProject>
}
