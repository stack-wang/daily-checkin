package com.example.checkin.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkin_records",
    foreignKeys = [
        ForeignKey(
            entity = CheckInProject::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["project_id", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["status", "project_id"])
    ]
)
data class CheckInRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "project_id")
    val projectId: Long,
    val date: String,
    val status: String = STATUS_NORMAL,
    val note: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_NORMAL = "NORMAL"
        const val STATUS_MAKEUP = "MAKEUP"
    }
}
