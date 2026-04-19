package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.exception.BadRequestException;
import ru.urfu.slivky.exception.NotFoundException;
import ru.urfu.slivky.model.*;
import ru.urfu.slivky.repository.TaskApplicationRepository;
import ru.urfu.slivky.repository.TaskRepository;
import ru.urfu.slivky.security.CurrentUser;
import ru.urfu.slivky.web.dto.MyApplicationResponse;
import ru.urfu.slivky.web.dto.TaskApplicationRequest;
import ru.urfu.slivky.web.dto.TaskApplicationResponse;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskApplicationService {

    private final TaskRepository taskRepository;
    private final TaskApplicationRepository applicationRepository;

    @Transactional
    public TaskApplicationResponse apply(Long taskId, TaskApplicationRequest req) {
        User volunteer = CurrentUser.get();
        if (volunteer.getRole() != UserRole.VOLUNTEER) {
            throw new AccessDeniedException("Only volunteers can apply");
        }
        Task task = taskRepository.findDetailById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        if (task.getStatus() != TaskStatus.OPEN) {
            throw new BadRequestException("Task is not open for applications");
        }
        if (applicationRepository.existsByTaskIdAndVolunteerId(taskId, volunteer.getId())) {
            throw new BadRequestException("You already applied to this task");
        }

        TaskApplication app = new TaskApplication();
        app.setTask(task);
        app.setVolunteer(volunteer);
        app.setMessage(req.message());
        app.setStatus(ApplicationStatus.PENDING);
        applicationRepository.save(app);

        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public List<TaskApplicationResponse> listForTask(Long taskId) {
        Task task = taskRepository.findDetailById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User actor = CurrentUser.get();
        assertTaskOwner(task, actor);

        return applicationRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyApplicationResponse> listMine() {
        User volunteer = CurrentUser.get();
        if (volunteer.getRole() != UserRole.VOLUNTEER) {
            throw new AccessDeniedException("Only volunteers have applications");
        }
        return applicationRepository.findByVolunteer_IdOrderByCreatedAtDesc(volunteer.getId()).stream()
                .map(a -> new MyApplicationResponse(
                        a.getId(),
                        a.getTask().getId(),
                        a.getTask().getTitle(),
                        a.getMessage(),
                        a.getStatus(),
                        a.getCreatedAt()
                ))
                .toList();
    }

    private void assertTaskOwner(Task task, User actor) {
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }
        if (task.getCreatedBy() != null && Objects.equals(task.getCreatedBy().getId(), actor.getId())) {
            return;
        }
        if (task.getShelter() != null && task.getShelter().getCreatedBy() != null
                && Objects.equals(task.getShelter().getCreatedBy().getId(), actor.getId())) {
            return;
        }
        throw new AccessDeniedException("Only the task author or shelter representative can view applications");
    }

    private TaskApplicationResponse toResponse(TaskApplication app) {
        User v = app.getVolunteer();
        return new TaskApplicationResponse(
                app.getId(),
                v.getId(),
                v.getFullName(),
                v.getEmail(),
                app.getMessage(),
                app.getStatus(),
                app.getCreatedAt()
        );
    }
}
