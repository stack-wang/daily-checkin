package com.example.checkin.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.checkin.CheckInApp
import com.example.checkin.MainActivity
import com.example.checkin.data.repository.CheckInRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CheckInRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val projects = repository.getProjectsWithReminder()
        if (projects.isEmpty()) return Result.success()

        val today = repository.today()
        val now = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        for (project in projects) {
            if (project.reminderTime.isEmpty()) continue

            val reminderTime = try {
                LocalTime.parse(project.reminderTime, timeFormatter)
            } catch (e: Exception) {
                continue
            }

            if (now.hour < reminderTime.hour ||
                (now.hour == reminderTime.hour && now.minute < reminderTime.minute)) {
                continue
            }

            val existingRecords = repository.getRecordsByDate(today)
            val hasCheckedIn = existingRecords.any { it.projectId == project.id }
            if (hasCheckedIn) continue

            sendNotification(project.name, project.id)
        }

        return Result.success()
    }

    private fun sendNotification(projectName: String, projectId: Long) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("project_id", projectId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            projectId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CheckInApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("记得打卡哦")
            .setContentText("「$projectName」还没有打卡，点击前往打卡")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(applicationContext)
                .notify("reminder_$projectId", projectId.toInt(), notification)
        }
    }
}
