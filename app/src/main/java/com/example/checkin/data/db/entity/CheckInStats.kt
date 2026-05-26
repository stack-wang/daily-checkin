package com.example.checkin.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkin_stats")
data class CheckInStats(
    @PrimaryKey
    @ColumnInfo(name = "project_id")
    val projectId: Long,
    @ColumnInfo(name = "total_count")
    val totalCount: Int = 0,
    @ColumnInfo(name = "makeup_count")
    val makeupCount: Int = 0,
    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,
    @ColumnInfo(name = "longest_streak")
    val longestStreak: Int = 0,
    @ColumnInfo(name = "last_checkin_date")
    val lastCheckInDate: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "total_reward")
    val totalReward: Int = 0
)
