package com.example.checkin.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ExactAlarmDialog(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要精确闹钟权限") },
        text = {
            Text(
                "为了准时收到打卡提醒，需要开启「闹钟和提醒」权限。" +
                        "点击下方按钮前往系统设置，找到「每日打卡」→ 打开开关。"
            )
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后")
            }
        }
    )
}
