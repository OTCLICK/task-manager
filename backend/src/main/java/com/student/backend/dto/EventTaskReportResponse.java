package com.student.backend.dto;

import com.student.backend.model.EventStatus;

import java.util.List;
import java.util.Map;

public record EventTaskReportResponse(
        String eventId,
        String eventName,
        EventStatus eventStatus,
        Map<String, Long> tasksByStatus,
        List<TaskReportLine> tasks
) {
    public record TaskReportLine(
            String taskId,
            String title,
            String status,
            String zoneName,
            int performerCount
    ) {}
}
