package com.example.checkin.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HeatmapChart(
    dailyCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val startDate = today.minusMonths(3).withDayOfMonth(1)
    val endDate = today

    val allDates = remember(dailyCounts) {
        val dates = mutableListOf<LocalDate>()
        var d = startDate
        while (!d.isAfter(endDate)) {
            dates.add(d)
            d = d.plusDays(1)
        }
        dates
    }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val maxCount = remember(dailyCounts) {
        dailyCounts.values.maxOrNull() ?: 1
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("少", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            listOf(
                Color(0xFFEBEDF0), Color(0xFF9BE9A8),
                Color(0xFF40C463), Color(0xFF30A14E), Color(0xFF216E39)
            ).forEach { color ->
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawRoundRect(
                        color = color,
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text("多", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        val cellSize = 14.dp
        val cellSpacing = 3.dp

        val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
        val numWeeks = ChronoUnit.WEEKS.between(
            startDate.with(DayOfWeek.MONDAY),
            endDate.with(DayOfWeek.SUNDAY)
        ).toInt() + 2

        Row {
            Column(modifier = Modifier.padding(end = 4.dp)) {
                weekLabels.forEach { label ->
                    Box(
                        modifier = Modifier.size(cellSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(start = 2.dp)
            ) {
                for (week in 0 until numWeeks) {
                    Column(modifier = Modifier.padding(horizontal = cellSpacing / 2)) {
                        for (dayOfWeek in 0..6) {
                            val date = startDate.plusWeeks(week.toLong())
                                .with(DayOfWeek.of(if (dayOfWeek == 6) 7 else dayOfWeek + 1))
                            val dateStr = date.format(formatter)
                            val count = dailyCounts[dateStr] ?: 0
                            val color = when {
                                count == 0 -> Color(0xFFEBEDF0)
                                maxCount <= 1 -> Color(0xFF40C463)
                                else -> {
                                    val ratio = count.toFloat() / maxCount.toFloat()
                                    when {
                                        ratio <= 0.25f -> Color(0xFF9BE9A8)
                                        ratio <= 0.5f -> Color(0xFF40C463)
                                        ratio <= 0.75f -> Color(0xFF30A14E)
                                        else -> Color(0xFF216E39)
                                    }
                                }
                            }

                            Canvas(modifier = Modifier.size(cellSize)) {
                                drawRoundRect(
                                    color = color,
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
