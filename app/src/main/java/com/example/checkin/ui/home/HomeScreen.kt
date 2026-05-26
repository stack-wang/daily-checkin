package com.example.checkin.ui.home

import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.db.entity.CheckInStats
import com.example.checkin.ui.theme.CheckGreen
import com.example.checkin.ui.theme.StreakGold
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        val today = LocalDate.now()
        val dayOfWeek = when (today.dayOfWeek.value) {
            1 -> "星期一"; 2 -> "星期二"; 3 -> "星期三"
            4 -> "星期四"; 5 -> "星期五"; 6 -> "星期六"; 7 -> "星期日"
            else -> ""
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column {
                    Text(
                        "${today.monthValue}月${today.dayOfMonth}日",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        dayOfWeek,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(uiState.projects, key = { it.id }) { project ->
                val isChecked = uiState.todayRecords.any { it.projectId == project.id }
                val stats = uiState.projectStats[project.id]
                HabitCard(
                    project = project,
                    isChecked = isChecked,
                    stats = stats,
                    onCheck = { viewModel.checkIn(project.id) },
                    onUncheck = { viewModel.uncheckIn(project.id) }
                )
            }

            val missedProjects = uiState.missedItems.filter { it.value.isNotEmpty() }
            if (missedProjects.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "补打卡（最近${uiState.makeUpDays}天）",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                for ((projectId, missedDates) in missedProjects) {
                    val project = uiState.projects.find { it.id == projectId } ?: continue
                    for (date in missedDates) {
                        item(key = "missed_${projectId}_$date") {
                            val localDate = try {
                                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            } catch (e: Exception) { null }
                            val displayDate = localDate?.let { "${it.monthValue}月${it.dayOfMonth}日" } ?: date

                            MissedCard(
                                projectName = project.name,
                                projectColor = Color(AndroidColor.parseColor(project.color)),
                                date = displayDate,
                                onMakeUp = { viewModel.makeUpCheckIn(project.id, date) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun HabitCard(
    project: CheckInProject,
    isChecked: Boolean,
    stats: CheckInStats?,
    onCheck: () -> Unit,
    onUncheck: () -> Unit
) {
    val projectColor = Color(AndroidColor.parseColor(project.color))
    val cardBg by animateColorAsState(
        if (isChecked) projectColor.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
        label = "cardBg"
    )
    val streak = stats?.currentStreak ?: 0
    val todayReward = ((streak.takeIf { isChecked } ?: (streak + 1)) - 1) % 7 + 1
    val totalReward = stats?.totalReward ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isChecked) onUncheck() else onCheck() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked || project.rewardEnabled) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        project.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isChecked) projectColor else MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (streak > 0) {
                            Icon(Icons.Filled.LocalFireDepartment, null, tint = StreakGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("$streak 天", fontSize = 12.sp, color = StreakGold)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (project.reminderTime.isNotEmpty()) {
                            Text(project.reminderTime, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (isChecked) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(CheckGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, "已打卡", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("打卡", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            if (project.rewardEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                val totalDays = if (isChecked) streak else streak + 1
                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        projectColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp)
                    ).padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isChecked) "今日已赚" else "今天打卡可赚",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text("¥$todayReward", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = projectColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Row {
                        repeat(7) { i ->
                            Text(
                                if (i < todayReward) "●" else "○",
                                fontSize = 10.sp,
                                color = if (i < todayReward) projectColor else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                Text(
                    "累计 ¥$totalReward",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MissedCard(
    projectName: String,
    projectColor: Color,
    date: String,
    onMakeUp: () -> Unit
) {
    var madeUp by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = projectColor.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    projectName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = { madeUp = true; onMakeUp() },
                enabled = !madeUp
            ) {
                Text(
                    if (madeUp) "已补" else "补卡",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (madeUp) CheckGreen else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
