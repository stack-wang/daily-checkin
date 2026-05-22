package com.example.checkin.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.repository.CheckInRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderScheduler {

    private const val TAG = "CheckIn"

    fun schedule(context: Context, project: CheckInProject) {
        if (project.reminderTime.isEmpty()) {
            Log.d(TAG, "[Alarm] skip '${project.name}'(id=${project.id}): no reminderTime")
            return
        }

        val parts = project.reminderTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fireTime = sdf.format(calendar.time)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("project_id", project.id)
            putExtra("project_name", project.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, project.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.i(TAG, "[Alarm] ✓ setExactAndAllowWhileIdle '${project.name}'(id=${project.id}) at $fireTime")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                Log.i(TAG, "[Alarm] ✓ setExact '${project.name}'(id=${project.id}) at $fireTime")
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                Log.i(TAG, "[Alarm] ✓ setExact '${project.name}'(id=${project.id}) at $fireTime")
            }
        } catch (e: SecurityException) {
            try {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.w(TAG, "[Alarm] ⚠ setExact denied, fallback to setAlarmClock for '${project.name}'(id=${project.id}) at $fireTime")
            } catch (e2: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                Log.w(TAG, "[Alarm] ⚠ setAlarmClock also denied, fallback to set() for '${project.name}'(id=${project.id})")
            }
        }
    }

    fun cancel(context: Context, projectId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, projectId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "[Alarm] cancelled alarm for project id=$projectId")
    }

    suspend fun scheduleAll(context: Context, repository: CheckInRepository) {
        try {
            val projects = repository.getProjectsWithReminder()
            Log.i(TAG, "[Alarm] scheduleAll: found ${projects.size} project(s) with reminders")
            for (project in projects) {
                Log.d(TAG, "[Alarm] scheduleAll: processing '${project.name}'(id=${project.id}) time=${project.reminderTime} enabled=${project.reminderEnabled}")
                schedule(context, project)
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Alarm] scheduleAll failed", e)
        }
    }
}
