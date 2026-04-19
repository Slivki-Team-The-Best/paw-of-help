package ru.urfu.slivky.web.dto;

import ru.urfu.slivky.model.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * Отклик волонтёра с привязкой к задаче (список «мои отклики»).
 */
public record MyApplicationResponse(
        Long id,
        Long taskId,
        String taskTitle,
        String message,
        ApplicationStatus status,
        LocalDateTime createdAt
) {
}
