package com.example.checkin.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.checkin.data.db.entity.CheckInProject
import com.example.checkin.ui.theme.PresetColors
import com.example.checkin.ui.theme.PresetIcons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateProjectSheet(
    onDismiss: () -> Unit,
    viewModel: CreateProjectViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(PresetIcons[0]) }
    var selectedColor by remember { mutableStateOf(PresetColors[0]) }
    var reminderHourStr by remember { mutableStateOf("08") }
    var reminderMinuteStr by remember { mutableStateOf("00") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "新建打卡项目",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("项目名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("图标", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetIcons.forEach { icon ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedIcon == icon) Color(android.graphics.Color.parseColor(selectedColor))
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .then(
                                if (selectedIcon == icon) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                ) else Modifier
                            )
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("颜色", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(color)))
                            .then(
                                if (selectedColor == color) Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("提醒时间", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeAdjuster(current = reminderHourStr, onAdjust = { h -> reminderHourStr = h }, range = 0..23)
                Text(" : ", style = MaterialTheme.typography.headlineMedium)
                TimeAdjuster(current = reminderMinuteStr, onAdjust = { m -> reminderMinuteStr = m }, range = 0..59)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val time = "${reminderHourStr.padStart(2, '0')}:${reminderMinuteStr.padStart(2, '0')}"
                        viewModel.create(
                            CheckInProject(
                                name = name,
                                icon = selectedIcon,
                                color = selectedColor,
                                reminderTime = time,
                                reminderEnabled = time.isNotEmpty()
                            )
                        )
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        }
    }
}

@Composable
private fun TimeAdjuster(
    current: String,
    onAdjust: (String) -> Unit,
    range: IntRange
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(
            onClick = {
                val v = (current.toIntOrNull() ?: 0) + 1
                val adjusted = if (v > range.last) range.first else v
                onAdjust(adjusted.toString().padStart(2, '0'))
            }
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedTextField(
            value = current,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(2)
                if (filtered.isEmpty()) {
                    onAdjust("00")
                } else {
                    val v = filtered.toIntOrNull() ?: 0
                    val clamped = v.coerceIn(range.first, range.last)
                    onAdjust(clamped.toString().padStart(2, '0'))
                }
            },
            modifier = Modifier.width(64.dp),
            textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
            singleLine = true
        )
        TextButton(
            onClick = {
                val v = (current.toIntOrNull() ?: 0) - 1
                val adjusted = if (v < range.first) range.last else v
                onAdjust(adjusted.toString().padStart(2, '0'))
            }
        ) {
            Text("−", style = MaterialTheme.typography.titleMedium)
        }
    }
}
