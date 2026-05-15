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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final PushNotificationService pushNotificationService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ZoneRepository zoneRepository,
                       EventRepository eventRepository,
                       ParticipationRepository participationRepository,
                       PushNotificationService pushNotificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
        this.pushNotificationService = pushNotificationService;
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
        pushTaskCreated(savedTask);
        return toTaskResponse(savedTask);
    }

    public TaskResponse updateTask(String taskId, TaskUpdateRequest request, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
        String eventId = resolveEventId(task);
        assertOrganizerOrCoordinator(userId, eventId);

        Zone zone = null;
        if (request.zoneId() != null) {
            zone = zoneRepository.findById(request.zoneId())
                    .orElseThrow(() -> new NotFoundException("Зона не найдена"));
            if (!zone.getEvent().getId().equals(eventId)) {
                throw new ValidationException("Зона не относится к этому мероприятию");
            }
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.taskPriority() != null ? request.taskPriority() : TaskPriority.MEDIUM);
        task.setZone(zone);
        task.setDeadline(request.deadline());

        List<String> performerIds = request.performers() != null ? request.performers() : List.of();
        if (performerIds.isEmpty()) {
            task.getPerformers().clear();
        } else {
            List<User> performers = userRepository.findAllById(performerIds);
            if (performers.size() != performerIds.size()) {
                throw new NotFoundException("Один или несколько исполнителей не найдены");
            }
            for (String performerId : performerIds) {
                Participation performerParticipation = participationRepository
                        .findByUserIdAndEventId(performerId, eventId)
                        .orElseThrow(() -> new ValidationException("Исполнитель не участвует в мероприятии"));
                if (!UserRole.PERFORMER.equals(performerParticipation.getRole())) {
                    throw new ValidationException("Исполнитель должен иметь роль PERFORMER в этом мероприятии");
                }
            }
            task.getPerformers().clear();
            task.getPerformers().addAll(performers);
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        pushTaskUpdated(saved);
        return toTaskResponse(saved);
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
        pushTaskStatus(saved, newStatus);
        if (newStatus == TaskStatus.HELP_REQUESTED) {
            pushHelpRequestedToOtherPerformers(saved);
        }
        return toTaskResponse(saved);
    }

    public TaskResponse declineSelfFromTask(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
        assertAssignedPerformer(userId, task);
        task.getPerformers().removeIf(u -> u.getId().equals(userId));
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        User leaver = userRepository.findById(userId).orElse(null);
        String who = leaver != null ? leaver.getEmail() : userId;
        notifyCoordinatorTaskEvent(saved, "TASK_PERFORMER_LEFT", who + " снялся с задачи");
        return toTaskResponse(saved);
    }

    public TaskResponse joinAsPerformerOnTask(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
        String eventId = resolveEventId(task);
        Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));
        if (participation.getRole() != UserRole.PERFORMER) {
            throw new AccessDeniedException("Присоединиться к задаче может только исполнитель мероприятия");
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw new ValidationException("Нельзя присоединиться к завершённой или отменённой задаче");
        }
        if (task.getPerformers() != null
                && task.getPerformers().stream().anyMatch(u -> u.getId().equals(userId))) {
            return toTaskResponse(task);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        task.getPerformers().add(user);
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        String zoneId = zoneIdForMute(saved);
        Map<String, String> data = baseTaskData(saved, "TASK_PERFORMER_JOINED");
        data.put("joinedUserId", userId);
        pushNotificationService.sendToUserRespectingZoneMute(
                saved.getCoordinator().getId(), zoneId, "К задаче присоединился исполнитель", saved.getTitle(), data);
        for (User peer : saved.getPerformers()) {
            if (!peer.getId().equals(userId)) {
                pushNotificationService.sendToUserRespectingZoneMute(
                        peer.getId(), zoneId, "Новый исполнитель на задаче", saved.getTitle(), data);
            }
        }
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

    public void deleteTask(String id, String userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
        String eventId = resolveEventId(task);
        assertOrganizerOrCoordinator(userId, eventId);
        taskRepository.deleteById(id);
    }

    private void assertOrganizerOrCoordinator(String userId, String eventId) {
        Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));
        if (participation.getRole() != UserRole.ORGANIZER && participation.getRole() != UserRole.COORDINATOR) {
            throw new AccessDeniedException("Только организатор и координатор могут изменять задачи");
        }
    }

    private void assertAssignedPerformer(String userId, Task task) {
        String eventId = resolveEventId(task);
        Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));
        if (participation.getRole() != UserRole.PERFORMER) {
            throw new AccessDeniedException("Только исполнитель может сняться с задачи");
        }
        boolean assigned = task.getPerformers() != null
                && task.getPerformers().stream().anyMatch(u -> u.getId().equals(userId));
        if (!assigned) {
            throw new ValidationException("Вы не назначены на эту задачу");
        }
    }

    private String zoneIdForMute(Task task) {
        return task.getZone() != null ? task.getZone().getId() : null;
    }

    private Map<String, String> baseTaskData(Task task, String type) {
        Map<String, String> m = new HashMap<>();
        m.put("type", type);
        m.put("eventId", resolveEventId(task));
        m.put("taskId", task.getId());
        m.put("zoneId", task.getZone() != null ? task.getZone().getId() : "");
        m.put("title", task.getTitle() != null ? task.getTitle() : "");
        return m;
    }

    private void pushTaskCreated(Task saved) {
        if (saved.getPerformers() == null || saved.getPerformers().isEmpty()) {
            return;
        }
        String z = zoneIdForMute(saved);
        Map<String, String> data = baseTaskData(saved, "TASK_CREATED");
        for (User p : saved.getPerformers()) {
            pushNotificationService.sendToUserRespectingZoneMute(
                    p.getId(), z, "Новая задача", saved.getTitle(), data);
        }
    }

    private void pushTaskUpdated(Task saved) {
        String z = zoneIdForMute(saved);
        Map<String, String> data = baseTaskData(saved, "TASK_UPDATED");
        if (saved.getPerformers() != null) {
            for (User p : saved.getPerformers()) {
                pushNotificationService.sendToUserRespectingZoneMute(
                        p.getId(), z, "Задача обновлена", saved.getTitle(), data);
            }
        }
        pushNotificationService.sendToUserRespectingZoneMute(
                saved.getCoordinator().getId(), z, "Задача обновлена", saved.getTitle(), data);
    }

    private void pushTaskStatus(Task saved, TaskStatus newStatus) {
        String z = zoneIdForMute(saved);
        Map<String, String> data = baseTaskData(saved, "TASK_STATUS");
        data.put("status", newStatus.name());
        String body = saved.getTitle() + " → " + newStatus.name();
        if (saved.getPerformers() != null) {
            for (User p : saved.getPerformers()) {
                pushNotificationService.sendToUserRespectingZoneMute(
                        p.getId(), z, "Статус задачи", body, data);
            }
        }
        pushNotificationService.sendToUserRespectingZoneMute(
                saved.getCoordinator().getId(), z, "Статус задачи", body, data);
    }

    private void pushHelpRequestedToOtherPerformers(Task saved) {
        String eventId = resolveEventId(saved);
        String z = zoneIdForMute(saved);
        Set<String> onTask = new HashSet<>();
        if (saved.getPerformers() != null) {
            for (User u : saved.getPerformers()) {
                onTask.add(u.getId());
            }
        }
        Map<String, String> data = baseTaskData(saved, "HELP_REQUESTED_TASK");
        data.put("status", TaskStatus.HELP_REQUESTED.name());
        String eventTitle = saved.getEvent() != null ? saved.getEvent().getName() : "Мероприятие";
        String body = "Запрошена помощь: " + saved.getTitle();
        for (Participation part : participationRepository.findByEvent_IdAndRole(eventId, UserRole.PERFORMER)) {
            String uid = part.getUser().getId();
            if (onTask.contains(uid)) {
                continue;
            }
            pushNotificationService.sendToUserRespectingZoneMute(
                    uid, z, eventTitle + ": нужна помощь", body, data);
        }
    }

    private void notifyCoordinatorTaskEvent(Task task, String type, String body) {
        String z = zoneIdForMute(task);
        Map<String, String> data = baseTaskData(task, type);
        pushNotificationService.sendToUserRespectingZoneMute(
                task.getCoordinator().getId(), z, "Задача", body, data);
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
