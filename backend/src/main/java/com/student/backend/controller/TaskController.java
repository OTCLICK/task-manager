package com.student.backend.controller;

import com.student.backend.dto.TaskCreateRequest;
import com.student.backend.dto.TaskResponse;
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

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    // TODO: Требуется авторизация - только координатор может создавать задачи
    // Временно используем параметр coordinatorId для тестирования
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @RequestParam String coordinatorId,
            @RequestParam(required = false) String zoneId // опциональный параметр
    ) {
        TaskResponse task = taskService.createTask(request, coordinatorId, zoneId);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        // TODO: Требуется авторизация - только создатель задачи может её удалить
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
