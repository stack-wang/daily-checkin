package com.example.checkin.worker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.checkin.CheckInApp
import com.example.checkin.MainActivity
import com.example.checkin.data.repository.CheckInRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val projectId = intent.getLongExtra("project_id", -1L)
        val projectName = intent.getStringExtra("project_name") ?: ""
        if (projectId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                ReminderEntryPoint::class.java
            )
            val repository = entryPoint.repository()
            val appContext = context.applicationContext

            val today = repository.today()
            val records = repository.getRecordsByDate(today)
            val hasCheckedIn = records.any { it.projectId == projectId }

            if (!hasCheckedIn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(
                    appContext, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                withContext(Dispatchers.Main) {
                    sendNotification(appContext, projectName, projectId)
                }
            }

            val project = repository.getProject(projectId)
            if (project != null) {
                ReminderScheduler.schedule(appContext, project)
            }

            pendingResult.finish()
        }
    }

    private fun sendNotification(context: Context, projectName: String, projectId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("project_id", projectId)
        }
        val pi = PendingIntent.getActivity(
            context, projectId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CheckInApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("记得打卡哦")
            .setContentText("「$projectName」还没有打卡，点击前往打卡")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context)
            .notify("reminder_$projectId", projectId.toInt(), notification)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun repository(): CheckInRepository
}
