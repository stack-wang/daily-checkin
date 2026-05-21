package com.example.checkin.worker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CheckIn"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val projectId = intent.getLongExtra("project_id", -1L)
        val projectName = intent.getStringExtra("project_name") ?: ""
        Log.i(TAG, "[Receiver] ⏰ FIRED! project='$projectName' id=$projectId")

        if (projectId == -1L) {
            Log.e(TAG, "[Receiver] invalid projectId, abort")
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ReminderEntryPoint::class.java
                )
                val repository = entryPoint.repository()
                val appContext = context.applicationContext

                val today = repository.today()
                Log.d(TAG, "[Receiver] today=$today, checking records...")
                val records = repository.getRecordsByDate(today)
                val hasCheckedIn = records.any { it.projectId == projectId }
                Log.i(TAG, "[Receiver] project='$projectName' id=$projectId: checkedIn=$hasCheckedIn (${records.size} records today)")

                if (!hasCheckedIn) {
                    val canNotify = canSendNotification(appContext)
                    Log.i(TAG, "[Receiver] canSendNotification=$canNotify (sdk=${Build.VERSION.SDK_INT})")
                    if (canNotify) {
                        sendNotification(appContext, projectName, projectId)
                        Log.i(TAG, "[Receiver] ✅ notification sent for '$projectName'")
                    } else {
                        Log.w(TAG, "[Receiver] ❌ cannot send notification - permission not granted")
                    }
                } else {
                    Log.d(TAG, "[Receiver] already checked in, skip notification")
                }

                val project = repository.getProject(projectId)
                if (project != null) {
                    ReminderScheduler.schedule(appContext, project)
                    Log.d(TAG, "[Receiver] re-scheduled next alarm for '$projectName'")
                } else {
                    Log.w(TAG, "[Receiver] project id=$projectId no longer exists, cancel alarm")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Receiver] exception", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun canSendNotification(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "[Receiver] POST_NOTIFICATIONS permission: ${if (granted) "GRANTED" else "DENIED"}")
            return granted
        }
        return true
    }

    private fun sendNotification(context: Context, projectName: String, projectId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
