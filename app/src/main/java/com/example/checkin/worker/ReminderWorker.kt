package com.example.checkin.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.checkin.data.repository.CheckInRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CheckInRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        ReminderScheduler.scheduleAll(applicationContext, repository)
        return Result.success()
    }
}
