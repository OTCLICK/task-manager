package com.example.mobile.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelHelper {
    const val DEFAULT_CHANNEL_ID = "task_manager_default"

    fun ensureDefaultChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "Общие уведомления",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Приглашения и события мероприятий"
        }
        manager.createNotificationChannel(channel)
    }
}
