package com.example.checkin.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkin_projects")
data class CheckInProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String = "check_circle",
    val color: String = "#1976D2",
    @ColumnInfo(name = "reminder_time")
    val reminderTime: String = "",
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "reward_enabled")
    val rewardEnabled: Boolean = false,
    @ColumnInfo(name = "makeup_enabled")
    val makeUpEnabled: Boolean = true
)
