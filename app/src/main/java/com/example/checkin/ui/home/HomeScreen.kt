package com.example.checkin.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.data.db.entity.CheckInRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val today = LocalDate.now()
                    val dayOfWeek = when (today.dayOfWeek.value) {
                        1 -> "星期一"
                        2 -> "星期二"
                        3 -> "星期三"
                        4 -> "星期四"
                        5 -> "星期五"
                        6 -> "星期六"
                        7 -> "星期日"
                        else -> ""
                    }
                    Text("${today.monthValue}月${today.dayOfMonth}日  $dayOfWeek")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    Text(
                        "今日打卡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.projects, key = { it.id }) { project ->
                    val isChecked = uiState.todayRecords.any { it.projectId == project.id }
                    CheckInItemCard(
                        project = project,
                        isChecked = isChecked,
                        onCheck = { viewModel.checkIn(project.id) }
                    )
                }

                val missedProjects = uiState.missedItems.filter { it.value.isNotEmpty() }
                if (missedProjects.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "昨日漏打卡（可补${uiState.makeUpDays}天）",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    for ((projectId, missedDates) in missedProjects) {
                        val project = uiState.projects.find { it.id == projectId } ?: continue
                        for (date in missedDates) {
                            item(key = "missed_${projectId}_$date") {
                                MissedCheckInCard(
                                    project = project,
                                    date = date,
                                    onMakeUp = { viewModel.makeUpCheckIn(projectId, date) }
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CheckInItemCard(
    project: CheckInProject,
    isChecked: Boolean,
    onCheck: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isChecked) Color(android.graphics.Color.parseColor(project.color)).copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surface,
        label = "bg"
    )
    val cardColor by animateColorAsState(
        if (isChecked) Color(android.graphics.Color.parseColor(project.color)).copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "card"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isChecked) { onCheck() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isChecked) Color(android.graphics.Color.parseColor(project.color))
                        else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isChecked) Color(android.graphics.Color.parseColor(project.color))
                            else MaterialTheme.colorScheme.onSurface
                )
            }

            if (project.reminderTime.isNotEmpty()) {
                Text(
                    text = project.reminderTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(project.color)))
            )
        }
    }
}

@Composable
fun MissedCheckInCard(
    project: CheckInProject,
    date: String,
    onMakeUp: () -> Unit
) {
    var hasMadeUp by remember { mutableStateOf(false) }

    val localDate = try {
        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (e: Exception) {
        null
    }
    val displayDate = localDate?.let {
        "${it.monthValue}月${it.dayOfMonth}日"
    } ?: date

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasMadeUp) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (hasMadeUp) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = {
                    hasMadeUp = true
                    onMakeUp()
                },
                enabled = !hasMadeUp
            ) {
                Text(
                    if (hasMadeUp) "已补" else "补",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
