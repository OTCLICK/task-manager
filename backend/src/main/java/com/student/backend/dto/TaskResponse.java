package com.student.backend.dto;

import com.student.backend.model.TaskPriority;
import com.student.backend.model.TaskStatus;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

//Тут нужно еще подумать по поводу значений taskPriority и taskStatus при создании
@Relation(collectionRelation = "tasks", itemRelation = "task")
public class TaskResponse {
    private final String taskId;
    private final String title;
    private final String description;
    private final TaskPriority taskPriority;
    private final TaskStatus taskStatus;
    private final ZoneResponse zone;
    private final List<UserResponse> performers;
    private final UserResponse coordinator;
    private final LocalDateTime deadline;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime completedAt;

    public TaskResponse(String taskId, String title, String description, TaskPriority taskPriority, TaskStatus taskStatus,
                        ZoneResponse zone, List<UserResponse> performers, UserResponse coordinator, LocalDateTime deadline) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.taskPriority = taskPriority;
        this.taskStatus = taskStatus;
        this.zone = zone;
        this.performers = performers;
        this.coordinator = coordinator;
        this.deadline = deadline;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.completedAt = null;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getTaskPriority() {
        return taskPriority;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public ZoneResponse getZone() {
        return zone;
    }

    public List<UserResponse> getPerformers() {
        return performers;
    }

    public UserResponse getCoordinator() {
        return coordinator;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TaskResponse that = (TaskResponse) o;
        return Objects.equals(taskId, that.taskId) && Objects.equals(title, that.title) && Objects.equals(description, that.description) && taskPriority == that.taskPriority && taskStatus == that.taskStatus && Objects.equals(zone, that.zone) && Objects.equals(performers, that.performers) && Objects.equals(coordinator, that.coordinator) && Objects.equals(deadline, that.deadline) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, title, description, taskPriority, taskStatus, zone, performers, coordinator, deadline, createdAt, updatedAt, completedAt);
    }
}
