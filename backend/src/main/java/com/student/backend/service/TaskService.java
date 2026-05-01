package com.student.backend.service;

import com.student.backend.dto.*;
import com.student.backend.exception.AccessDeniedException;
import com.student.backend.exception.NotFoundException;
import com.student.backend.exception.ValidationException;
import com.student.backend.model.*;
import com.student.backend.repository.TaskRepository;
import com.student.backend.repository.UserRepository;
import com.student.backend.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ZoneRepository zoneRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
        return toTaskResponse(task);
    }

    public TaskResponse createTask(TaskCreateRequest request, String coordinatorId, String zoneId) {
        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new NotFoundException("Координатор не найден"));

        if (coordinator.getRole() != UserRole.COORDINATOR) {
            throw new AccessDeniedException("Только координатор может создавать задачи");
        }

        Task task = new Task(
                request.title(),
                request.description(),
                request.taskPriority() != null ? request.taskPriority() : TaskPriority.MEDIUM,
                coordinator,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        if (request.zoneId() != null) {
            Zone zone = zoneRepository.findById(request.zoneId())
                    .orElseThrow(() -> new NotFoundException("Зона не найдена"));
            task.setZone(zone);
        }

        task.setDeadline(request.deadline());

        if (request.performers() != null && !request.performers().isEmpty()) {
            List<User> performers = userRepository.findAllById(request.performers());
            if (performers.size() != request.performers().size()) {
                throw new NotFoundException("Один или несколько исполнителей не найдены");
            }
            for (User performer : performers) {
                if (performer.getRole() != UserRole.PERFORMER) {
                    throw new ValidationException("Исполнитель должен иметь роль PERFORMER");
                }
            }
            task.setPerformers(performers);
        }

        Task savedTask = taskRepository.save(task);
        return toTaskResponse(savedTask);
    }

    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new NotFoundException("Задача не найдена");
        }
        taskRepository.deleteById(id);
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                toZoneResponse(task.getZone()),
                task.getPerformers() != null ?
                        task.getPerformers().stream().map(this::toUserResponse).collect(Collectors.toList()) :
                        List.of(),
                toUserResponse(task.getCoordinator()),
                task.getDeadline()
        );
    }

    private ZoneResponse toZoneResponse(Zone zone) {
        if (zone == null) return null;
        return new ZoneResponse(
                zone.getId(),
                zone.getName(),
                zone.getDescription(),
                zone.getParticipatesCount(),
                toEventResponse(zone.getEvent()),
                toUserResponse(zone.getCoordinator())
        );
    }

    private EventResponse toEventResponse(Event event) {
        if (event == null) return null;
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getAddress(),
                event.getParticipatesCount(),
                event.getStatus(),
                event.getStartTime(),
                event.getEndTime(),
                toUserResponse(event.getOrganizer())
        );
    }

    private UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                new FullNameDto(
                        user.getFullName().getName(),
                        user.getFullName().getSurname(),
                        user.getFullName().getPatronymic()
                ),
                user.getRole()
        );
    }
}
