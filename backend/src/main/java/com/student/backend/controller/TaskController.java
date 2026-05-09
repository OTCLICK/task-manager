package com.student.backend.controller;

import com.student.backend.dto.TaskCreateRequest;
import com.student.backend.dto.TaskResponse;
import com.student.backend.dto.TaskStatusPatchRequest;
import com.student.backend.security.SecurityUtils;
import com.student.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    public TaskController(TaskService taskService, SecurityUtils securityUtils) {
        this.taskService = taskService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false) String eventId
    ) {
        List<TaskResponse> tasks = (eventId != null && !eventId.isBlank())
                ? taskService.getTasksByEventId(eventId)
                : taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request
    ) {
        String coordinatorId = securityUtils.getCurrentUserId();
        TaskResponse task = taskService.createTask(request, coordinatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String id,
            @Valid @RequestBody TaskStatusPatchRequest request
    ) {
        String userId = securityUtils.getCurrentUserId();
        TaskResponse task = taskService.updateTaskStatus(id, request.taskStatus(), userId);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        String coordinatorId = securityUtils.getCurrentUserId();
        taskService.deleteTask(id, coordinatorId);
        return ResponseEntity.noContent().build();
    }
}
