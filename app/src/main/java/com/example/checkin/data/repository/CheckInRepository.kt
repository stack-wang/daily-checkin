package com.example.checkin.data.repository

import com.example.checkin.data.db.dao.ProjectDao
import com.example.checkin.data.db.dao.RecordDao
import com.example.checkin.data.db.dao.StatsDao
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.db.entity.CheckInRecord
import com.example.checkin.data.db.entity.CheckInStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val recordDao: RecordDao,
    private val statsDao: StatsDao
) {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val makeUpDaysFlow = MutableStateFlow(7)

    fun observeProjects(): Flow<List<CheckInProject>> = projectDao.observeAll()

    fun observeStats(): Flow<List<CheckInStats>> = statsDao.observeAll()

    suspend fun getAllStats(): List<CheckInStats> = statsDao.getAll()

    suspend fun getAllProjects(): List<CheckInProject> = projectDao.getAll()

    suspend fun getProject(id: Long): CheckInProject? = projectDao.getById(id)

    suspend fun createProject(project: CheckInProject): Long {
        val maxOrder = projectDao.getMaxSortOrder()
        val id = projectDao.insert(project.copy(sortOrder = maxOrder + 1))
        statsDao.upsert(CheckInStats(projectId = id))
        return id
    }

    suspend fun updateProject(project: CheckInProject) = projectDao.update(project)

    suspend fun deleteProject(project: CheckInProject) {
        recordDao.deleteByProject(project.id)
        statsDao.deleteByProject(project.id)
        projectDao.delete(project)
    }

    fun observeRecordsByDate(date: String): Flow<List<CheckInRecord>> =
        recordDao.observeByDate(date)

    suspend fun getRecordsByDate(date: String): List<CheckInRecord> =
        recordDao.getByDate(date)

    suspend fun checkIn(projectId: Long, date: String, note: String? = null): Boolean {
        val existing = recordDao.getByProjectAndDate(projectId, date)
        if (existing != null) return false

        recordDao.insert(
            CheckInRecord(
                projectId = projectId,
                date = date,
                status = CheckInRecord.STATUS_NORMAL,
                note = note
            )
        )
        updateStatsForProject(projectId)
        return true
    }

    suspend fun makeUpCheckIn(projectId: Long, date: String): Boolean {
        if (date == today()) return checkIn(projectId, date)

        val existing = recordDao.getByProjectAndDate(projectId, date)
        if (existing != null) return false

        recordDao.insert(
            CheckInRecord(
                projectId = projectId,
                date = date,
                status = CheckInRecord.STATUS_MAKEUP
            )
        )
        updateStatsForProject(projectId)
        return true
    }

    suspend fun uncheckIn(projectId: Long, date: String) {
        recordDao.deleteByProjectAndDate(projectId, date)
        updateStatsForProject(projectId)
    }

    suspend fun getDailyCountInRange(startDate: String, endDate: String): List<RecordDao.DailyCount> =
        recordDao.getDailyCountInRange(startDate, endDate)

    suspend fun getCheckedDates(projectId: Long, startDate: String, endDate: String): List<String> =
        recordDao.getCheckedDates(projectId, startDate, endDate)

    suspend fun getMissedCheckIns(project: CheckInProject): List<String> {
        if (!project.makeUpEnabled) return emptyList()

        val today = LocalDate.parse(today(), dateFormatter)
        val ndaysAgo = today.minusDays(makeUpDaysFlow.value.toLong())
        val projectCreateDate = Instant.ofEpochMilli(project.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val effectiveStart = if (projectCreateDate.isAfter(ndaysAgo)) projectCreateDate else ndaysAgo
        val startDate = effectiveStart.format(dateFormatter)
        val records = recordDao.getByProjectAndDateRange(project.id, startDate, today.format(dateFormatter))
        val recordedDates = records.map { it.date }.toSet()

        val missedDates = mutableListOf<String>()
        var d = effectiveStart
        while (d < today) {
            val dateStr = d.format(dateFormatter)
            if (dateStr !in recordedDates) {
                missedDates.add(dateStr)
            }
            d = d.plusDays(1)
        }
        return missedDates
    }

    suspend fun getProjectsWithReminder(): List<CheckInProject> =
        projectDao.getProjectsWithReminder()

    suspend fun updateProjectSortOrder(id: Long, sortOrder: Int) =
        projectDao.updateSortOrder(id, sortOrder)

    private suspend fun updateStatsForProject(projectId: Long) {
        val allRecords = recordDao.getByProject(projectId)
        val totalCount = allRecords.size
        val makeupCount = allRecords.count { it.status == CheckInRecord.STATUS_MAKEUP }

        val sortedDates = allRecords
            .filter { it.status == CheckInRecord.STATUS_NORMAL }
            .map { LocalDate.parse(it.date, dateFormatter) }
            .sortedDescending()

        val lastCheckInDate = if (sortedDates.isNotEmpty()) sortedDates.first().format(dateFormatter) else null

        var currentStreak = 0
        val todayLocal = LocalDate.parse(today(), dateFormatter)

        if (sortedDates.isNotEmpty()) {
            if (sortedDates.first() == todayLocal) {
                currentStreak = 1
                for (i in 0 until sortedDates.size - 1) {
                    val diff = sortedDates[i].until(sortedDates[i + 1], ChronoUnit.DAYS)
                    if (diff == -1L) {
                        currentStreak++
                    } else {
                        break
                    }
                }
            }
        }

        var longestStreak = 0
        if (sortedDates.isNotEmpty()) {
            var streak = 1
            for (i in 0 until sortedDates.size - 1) {
                val diff = sortedDates[i].until(sortedDates[i + 1], ChronoUnit.DAYS)
                if (diff == -1L) {
                    streak++
                } else {
                    if (streak > longestStreak) longestStreak = streak
                    streak = 1
                }
            }
            if (streak > longestStreak) longestStreak = streak
        }

        val stats = CheckInStats(
            projectId = projectId,
            totalCount = totalCount,
            makeupCount = makeupCount,
            currentStreak = currentStreak,
            longestStreak = maxOf(currentStreak, longestStreak),
            lastCheckInDate = lastCheckInDate,
            updatedAt = System.currentTimeMillis(),
            totalReward = calculateTotalReward(allRecords)
        )
        statsDao.upsert(stats)
    }

    private fun calculateTotalReward(records: List<CheckInRecord>): Int {
        val normalDates = records
            .filter { it.status == CheckInRecord.STATUS_NORMAL }
            .map { LocalDate.parse(it.date, dateFormatter) }
            .sorted()
        if (normalDates.isEmpty()) return 0

        var streak = 1
        var totalReward = ((streak - 1) % 7) + 1
        for (i in 1 until normalDates.size) {
            val diff = normalDates[i - 1].until(normalDates[i], ChronoUnit.DAYS).toInt()
            if (diff == 1) {
                streak++
            } else {
                streak = 1
            }
            totalReward += ((streak - 1) % 7) + 1
        }
        return totalReward
    }

    fun today(): String = LocalDate.now().format(dateFormatter)

    companion object {
        fun todayStatic(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}
