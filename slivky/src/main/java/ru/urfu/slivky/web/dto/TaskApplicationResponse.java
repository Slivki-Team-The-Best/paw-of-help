package ru.urfu.slivky.web.dto;

import ru.urfu.slivky.model.ApplicationStatus;

import java.time.LocalDateTime;

public record TaskApplicationResponse(
        Long id,
        Long volunteerId,
        String volunteerName,
        String volunteerEmail,
        String message,
        ApplicationStatus status,
        LocalDateTime createdAt
) {
}
