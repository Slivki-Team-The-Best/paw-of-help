package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.exception.BadRequestException;
import ru.urfu.slivky.exception.NotFoundException;
import ru.urfu.slivky.model.*;
import ru.urfu.slivky.repository.*;
import ru.urfu.slivky.security.CurrentUser;
import ru.urfu.slivky.web.dto.SkillDto;
import ru.urfu.slivky.web.dto.TaskCreateRequest;
import ru.urfu.slivky.web.dto.TaskResponse;
import ru.urfu.slivky.web.dto.TaskUpdateRequest;
import ru.urfu.slivky.web.dto.VolunteerMatchResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SkillRepository skillRepository;
    private final AnimalRepository animalRepository;
    private final ShelterRepository shelterRepository;
    private final MatchingService matchingService;

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Задачи, созданные текущим пользователем (владелец животного / автор запроса).
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> listMyTasks() {
        User actor = CurrentUser.get();
        return taskRepository.findByCreatedByIdOrderByCreatedAtDesc(actor.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        return toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest req) {
        User actor = CurrentUser.get();
        if (!(actor.getRole() == UserRole.PET_OWNER
                || actor.getRole() == UserRole.SHELTER_STAFF
                || actor.getRole() == UserRole.ADMIN)) {
            throw new AccessDeniedException("Only pet owners or shelter staff can create tasks");
        }

        Task task = new Task();
        task.setTitle(req.title().trim());
        task.setDescription(req.description());
        task.setTaskType(req.taskType());
        task.setPriority(req.priority() != null ? req.priority() : Priority.NORMAL);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(actor);
        task.setAddress(req.address());
        task.setScheduledStart(req.scheduledStart());
        task.setScheduledEnd(req.scheduledEnd());

        if (req.animalId() != null) {
            Animal animal = animalRepository.findById(req.animalId())
                    .orElseThrow(() -> new BadRequestException("Animal not found"));
            task.setAnimal(animal);
        }
        if (req.shelterId() != null) {
            Shelter shelter = shelterRepository.findById(req.shelterId())
                    .orElseThrow(() -> new BadRequestException("Shelter not found"));
            task.setShelter(shelter);
        }

        applySkills(task, req.requiredSkillIds());
        taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest req) {
        Task task = taskRepository.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User actor = CurrentUser.get();
        assertCanEdit(task, actor);

        if (req.title() != null) {
            task.setTitle(req.title().trim());
        }
        if (req.description() != null) {
            task.setDescription(req.description());
        }
        if (req.taskType() != null) {
            task.setTaskType(req.taskType());
        }
        if (req.status() != null) {
            task.setStatus(req.status());
        }
        if (req.priority() != null) {
            task.setPriority(req.priority());
        }
        if (req.address() != null) {
            task.setAddress(req.address());
        }
        if (req.scheduledStart() != null) {
            task.setScheduledStart(req.scheduledStart());
        }
        if (req.scheduledEnd() != null) {
            task.setScheduledEnd(req.scheduledEnd());
        }
        if (req.animalId() != null) {
            if (req.animalId() == 0) {
                task.setAnimal(null);
            } else {
                Animal animal = animalRepository.findById(req.animalId())
                        .orElseThrow(() -> new BadRequestException("Animal not found"));
                task.setAnimal(animal);
            }
        }
        if (req.shelterId() != null) {
            if (req.shelterId() == 0) {
                task.setShelter(null);
            } else {
                Shelter shelter = shelterRepository.findById(req.shelterId())
                        .orElseThrow(() -> new BadRequestException("Shelter not found"));
                task.setShelter(shelter);
            }
        }
        if (req.requiredSkillIds() != null) {
            applySkills(task, req.requiredSkillIds());
        }

        return toResponse(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User actor = CurrentUser.get();
        assertCanEdit(task, actor);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<VolunteerMatchResponse> getMatches(Long taskId) {
        Task task = taskRepository.findDetailById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User actor = CurrentUser.get();
        assertCanViewMatches(task, actor);
        return matchingService.matchVolunteers(task);
    }

    private void assertCanEdit(Task task, User actor) {
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }
        if (task.getCreatedBy() != null && Objects.equals(task.getCreatedBy().getId(), actor.getId())) {
            return;
        }
        if (isShelterRepresentative(task, actor)) {
            return;
        }
        throw new AccessDeniedException("You cannot edit this task");
    }

    private void assertCanViewMatches(Task task, User actor) {
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }
        if (task.getCreatedBy() != null && Objects.equals(task.getCreatedBy().getId(), actor.getId())) {
            return;
        }
        if (isShelterRepresentative(task, actor)) {
            return;
        }
        throw new AccessDeniedException("Only the task author or shelter representative can view matches");
    }

    /**
     * Представитель приюта — пользователь, создавший карточку приюта (учётная запись НКО).
     */
    private boolean isShelterRepresentative(Task task, User actor) {
        if (task.getShelter() == null || task.getShelter().getCreatedBy() == null) {
            return false;
        }
        return Objects.equals(task.getShelter().getCreatedBy().getId(), actor.getId());
    }

    private void applySkills(Task task, List<Long> skillIds) {
        task.getRequiredSkills().clear();
        if (skillIds == null || skillIds.isEmpty()) {
            return;
        }
        Set<Long> unique = new HashSet<>(skillIds);
        List<Skill> skills = skillRepository.findAllById(unique);
        if (skills.size() != unique.size()) {
            throw new BadRequestException("Some skill ids are invalid");
        }
        task.getRequiredSkills().addAll(skills);
    }

    private TaskResponse toResponse(Task task) {
        List<SkillDto> skills = task.getRequiredSkills().stream()
                .map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory()))
                .collect(Collectors.toList());

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getTaskType(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedBy() != null ? task.getCreatedBy().getId() : null,
                task.getCreatedBy() != null ? task.getCreatedBy().getFullName() : null,
                task.getAnimal() != null ? task.getAnimal().getId() : null,
                task.getShelter() != null ? task.getShelter().getId() : null,
                skills,
                task.getAddress(),
                task.getScheduledStart(),
                task.getScheduledEnd(),
                task.getCreatedAt()
        );
    }
}
