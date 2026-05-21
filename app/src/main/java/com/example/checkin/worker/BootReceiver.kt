package com.example.checkin.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CheckIn"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "[BootReceiver] received: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        ReminderEntryPoint::class.java
                    )
                    ReminderScheduler.scheduleAll(context, entryPoint.repository())
                    Log.i(TAG, "[BootReceiver] ✅ alarms rescheduled after boot")
                } catch (e: Exception) {
                    Log.e(TAG, "[BootReceiver] failed", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
