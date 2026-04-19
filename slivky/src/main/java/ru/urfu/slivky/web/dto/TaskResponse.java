package ru.urfu.slivky.web.dto;

import ru.urfu.slivky.model.Priority;
import ru.urfu.slivky.model.TaskStatus;
import ru.urfu.slivky.model.TaskType;

import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskType taskType,
        TaskStatus status,
        Priority priority,
        Long createdById,
        String createdByName,
        Long animalId,
        Long shelterId,
        List<SkillDto> requiredSkills,
        String address,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        LocalDateTime createdAt
) {
}
