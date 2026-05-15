package com.example.mobile.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.mobile.MainActivity
import com.example.mobile.R
import com.example.mobile.data.api.ApiClient
import com.example.mobile.data.model.FcmTokenRequest
import com.example.mobile.utils.NotificationChannelHelper
import com.example.mobile.utils.TokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class TaskManagerFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            val jwt = withTimeoutOrNull(5_000) {
                TokenManager(applicationContext).tokenFlow
                    .first { !it.isNullOrBlank() }
            } ?: return@launch
            runCatching {
                ApiClient.createAuthorizedApiService(jwt).registerFcmToken(FcmTokenRequest(token))
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            message.data["eventId"]?.takeIf { it.isNotBlank() }?.let { putExtra("openEventId", it) }
            message.data["taskId"]?.takeIf { it.isNotBlank() }?.let { putExtra("openTaskId", it) }
            message.data["type"]?.takeIf { it.isNotBlank() }?.let { putExtra("pushType", it) }
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationChannelHelper.DEFAULT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((message.messageId ?: title).hashCode(), notification)
    }
}
