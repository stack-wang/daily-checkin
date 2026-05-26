package com.example.checkin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkin.ui.theme.CheckGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarGrid(
    checkedDates: Set<String>,
    modifier: Modifier = Modifier,
    months: Int = 1
) {
    val today = LocalDate.now()
    val startDate = today.minusMonths((months - 1).toLong()).withDayOfMonth(1)
    val endDate = today
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekLabels.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        var d = startDate
        val dayOfWeek = d.dayOfWeek.value - 1
        if (dayOfWeek > 0) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                repeat(dayOfWeek) { Spacer(modifier = Modifier.weight(1f)) }
                while (d <= endDate && d <= startDate.plusDays((7 - dayOfWeek - 1).toLong())) {
                    CalendarCell(
                        date = d,
                        checked = formatter.format(d) in checkedDates,
                        isToday = d == today
                    )
                    d = d.plusDays(1)
                }
            }
        }

        while (!d.isAfter(endDate)) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                val weekEnd = d.plusDays(6)
                val displayEnd = if (weekEnd.isAfter(endDate)) endDate else weekEnd
                val emptyBefore = (d.dayOfWeek.value - 1)
                repeat(emptyBefore) { Spacer(modifier = Modifier.weight(1f)) }
                var cur = d
                while (!cur.isAfter(displayEnd)) {
                    CalendarCell(
                        date = cur,
                        checked = formatter.format(cur) in checkedDates,
                        isToday = cur == today
                    )
                    cur = cur.plusDays(1)
                }
                val emptyAfter = 7 - (if (displayEnd.dayOfWeek.value == 7) 7 else displayEnd.dayOfWeek.value)
                repeat(emptyAfter) { Spacer(modifier = Modifier.weight(1f)) }
                d = displayEnd.plusDays(1)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CheckGreen))
            Text(" 已打卡", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
            Text(" 未打卡", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalendarCell(
    date: LocalDate,
    checked: Boolean,
    isToday: Boolean
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(1.dp)
            .clip(CircleShape)
            .background(
                when {
                    checked -> CheckGreen.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            date.dayOfMonth.toString(),
            fontSize = 12.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                checked -> CheckGreen
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )
    }
}
