package ru.urfu.slivky.web.dto;

import ru.urfu.slivky.model.Priority;
import ru.urfu.slivky.model.TaskStatus;
import ru.urfu.slivky.model.TaskType;

import java.time.LocalDateTime;
import java.util.List;

public record TaskUpdateRequest(
        String title,
        String description,
        TaskType taskType,
        TaskStatus status,
        Priority priority,
        List<Long> requiredSkillIds,
        String address,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        Long animalId,
        Long shelterId
) {
}
