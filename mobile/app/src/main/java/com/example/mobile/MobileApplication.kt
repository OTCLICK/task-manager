package com.example.mobile

import android.app.Application
import com.example.mobile.data.api.ApiClient
import com.example.mobile.data.model.FcmTokenRequest
import com.example.mobile.utils.NotificationChannelHelper
import com.example.mobile.utils.TokenManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MobileApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NotificationChannelHelper.ensureDefaultChannel(this)

        val tokenManager = TokenManager(this)
        applicationScope.launch {
            tokenManager.tokenFlow.distinctUntilChanged().collect { jwt ->
                if (jwt.isNullOrBlank()) return@collect
                runCatching {
                    val fcm = FirebaseMessaging.getInstance().token.await()
                    ApiClient.createAuthorizedApiService(jwt).registerFcmToken(FcmTokenRequest(fcm))
                }
            }
        }
    }
}
