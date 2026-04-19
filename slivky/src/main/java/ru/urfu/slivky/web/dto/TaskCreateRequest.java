package ru.urfu.slivky.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.urfu.slivky.model.Priority;
import ru.urfu.slivky.model.TaskType;

import java.time.LocalDateTime;
import java.util.List;

public record TaskCreateRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @NotNull TaskType taskType,
        Priority priority,
        Long animalId,
        Long shelterId,
        List<Long> requiredSkillIds,
        String address,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd
) {
}
