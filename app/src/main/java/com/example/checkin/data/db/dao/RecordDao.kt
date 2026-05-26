package com.example.checkin.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.checkin.data.db.entity.CheckInRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("SELECT * FROM checkin_records WHERE date = :date ORDER BY id ASC")
    fun observeByDate(date: String): Flow<List<CheckInRecord>>

    @Query("SELECT * FROM checkin_records WHERE date = :date")
    suspend fun getByDate(date: String): List<CheckInRecord>

    @Query("SELECT * FROM checkin_records WHERE project_id = :projectId AND date = :date LIMIT 1")
    suspend fun getByProjectAndDate(projectId: Long, date: String): CheckInRecord?

    @Query("SELECT * FROM checkin_records WHERE project_id = :projectId ORDER BY date DESC")
    suspend fun getByProject(projectId: Long): List<CheckInRecord>

    @Query("""
        SELECT * FROM checkin_records 
        WHERE project_id = :projectId AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    suspend fun getByProjectAndDateRange(
        projectId: Long,
        startDate: String,
        endDate: String
    ): List<CheckInRecord>

    @Query("""
        SELECT date, COUNT(*) as count FROM checkin_records 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY date ORDER BY date ASC
    """)
    suspend fun getDailyCountInRange(startDate: String, endDate: String): List<DailyCount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CheckInRecord): Long

    @Query("DELETE FROM checkin_records WHERE project_id = :projectId AND date = :date")
    suspend fun deleteByProjectAndDate(projectId: Long, date: String)

    @Query("DELETE FROM checkin_records WHERE project_id = :projectId")
    suspend fun deleteByProject(projectId: Long)

    @Query("SELECT date FROM checkin_records WHERE project_id = :projectId AND status = 'NORMAL' AND date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getCheckedDates(projectId: Long, start: String, end: String): List<String>

    data class DailyCount(
        val date: String,
        val count: Int
    )
}
