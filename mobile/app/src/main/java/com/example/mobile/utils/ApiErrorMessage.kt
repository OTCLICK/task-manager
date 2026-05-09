package com.example.mobile.utils

import org.json.JSONObject
import retrofit2.Response

fun Response<*>.apiErrorMessage(): String? {
    val raw = errorBody()?.string()?.trim().orEmpty()
    if (raw.isEmpty()) return null
    return try {
        val json = JSONObject(raw)
        json.optString("message", "").takeIf { it.isNotBlank() }
            ?: json.optString("error", "").takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        raw.takeIf { it.isNotBlank() }
    }
}

fun Response<*>.toUserFacingHttpError(fallback: String = "Запрос отклонён сервером"): String {
    val detail = apiErrorMessage()
    val reason = message().takeIf { it.isNotBlank() }
    val base = detail ?: reason ?: fallback
    return "$base (HTTP ${code()})"
}
