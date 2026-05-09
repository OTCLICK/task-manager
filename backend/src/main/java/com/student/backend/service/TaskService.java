package com.student.backend.service;

import com.student.backend.dto.*;
import com.student.backend.exception.AccessDeniedException;
import com.student.backend.exception.NotFoundException;
import com.student.backend.exception.ValidationException;
import com.student.backend.model.*;
import com.student.backend.repository.*;
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
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ZoneRepository zoneRepository,
                       EventRepository eventRepository,
                       ParticipationRepository participationRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByEventId(String eventId) {
        return taskRepository.findAllByEventId(eventId).stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
        return toTaskResponse(task);
    }

    public TaskResponse createTask(TaskCreateRequest request, String coordinatorId/*, String zoneId*/) {
        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new NotFoundException("Координатор не найден"));

//        Zone zone = zoneRepository.findById(zoneId)
//                .orElseThrow(() -> new NotFoundException("Зона не найдена"));

//        if (coordinator.getRole() != UserRole.COORDINATOR) {
//            throw new AccessDeniedException("Только координатор может создавать задачи");
//        }

        Participation participation = participationRepository.findByUserIdAndEventId(coordinatorId, request.eventId())
                .orElseThrow(() -> new AccessDeniedException("Не участвуете в мероприятии"));

        if (participation.getRole() != UserRole.ORGANIZER && participation.getRole() != UserRole.COORDINATOR) {
            throw new AccessDeniedException("Только организатор и координатор могут создавать задачи");
        }

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));

        String zoneId = request.zoneId();

        Zone zone = null;
        if (zoneId != null) {
            zone = zoneRepository.findById(zoneId)
                    .orElseThrow(() -> new NotFoundException("Зона не найдена"));
            if (!zone.getEvent().getId().equals(request.eventId())) {
                throw new ValidationException("Зона не относится к этому мероприятию");
            }
        }

        Task task = new Task(
                request.title(),
                request.description(),
                request.taskPriority() != null ? request.taskPriority() : TaskPriority.MEDIUM,
                zone,
                coordinator,
                request.deadline(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        task.setEvent(event);

//        if (request.zoneId() != null) {
//            Zone zone = zoneRepository.findById(request.zoneId())
//                    .orElseThrow(() -> new NotFoundException("Зона не найдена"));
//            task.setZone(zone);
//        }
//
//        task.setDeadline(request.deadline());

        if (request.performers() != null && !request.performers().isEmpty()) {
            List<User> performers = userRepository.findAllById(request.performers());
            if (performers.size() != request.performers().size()) {
                throw new NotFoundException("Один или несколько исполнителей не найдены");
            }
            for (String performerId : request.performers()) {
                Participation performerParticipation = participationRepository
                        .findByUserIdAndEventId(performerId, request.eventId())
                        .orElseThrow(() -> new ValidationException("Исполнитель не участвует в мероприятии"));

                if (!UserRole.PERFORMER.equals(performerParticipation.getRole())) {
                    throw new ValidationException("Исполнитель должен иметь роль PERFORMER в этом мероприятии");
                }
            }
            task.setPerformers(performers);
        }

        Task savedTask = taskRepository.save(task);
        return toTaskResponse(savedTask);
    }

    public TaskResponse updateTaskStatus(String taskId, TaskStatus newStatus, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
        assertCanChangeTaskStatus(task, userId);
        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        if (newStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
        Task saved = taskRepository.save(task);
        return toTaskResponse(saved);
    }

    private void assertCanChangeTaskStatus(Task task, String userId) {
        if (task.getCoordinator().getId().equals(userId)) {
            return;
        }
        String eventId = resolveEventId(task);
        Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));
        if (participation.getRole() == UserRole.ORGANIZER || participation.getRole() == UserRole.COORDINATOR) {
            return;
        }
        if (participation.getRole() == UserRole.PERFORMER) {
            boolean assigned = task.getPerformers() != null
                    && task.getPerformers().stream().anyMatch(u -> u.getId().equals(userId));
            if (assigned) {
                return;
            }
        }
        throw new AccessDeniedException("Недостаточно прав для смены статуса задачи");
    }

    private String resolveEventId(Task task) {
        if (task.getEvent() != null) {
            return task.getEvent().getId();
        }
        if (task.getZone() != null && task.getZone().getEvent() != null) {
            return task.getZone().getEvent().getId();
        }
        throw new ValidationException("У задачи не задано мероприятие");
    }

    public void deleteTask(String id, String coordinatorId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));

        if (!task.getCoordinator().getId().equals(coordinatorId)) {
            throw new AccessDeniedException("Недостаточно прав для удаления задачи");
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
                )
//                user.getRole()
        );
    }
}
