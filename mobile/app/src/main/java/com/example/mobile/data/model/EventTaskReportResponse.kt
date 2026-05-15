package com.example.mobile.data.model

data class EventTaskReportResponse(
    val eventId: String,
    val eventName: String,
    val eventStatus: String,
    val tasksByStatus: Map<String, Long>,
    val tasks: List<TaskReportLine>
) {
    data class TaskReportLine(
        val taskId: String,
        val title: String,
        val status: String,
        val zoneName: String?,
        val performerCount: Int
    )
}
