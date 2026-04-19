package ru.urfu.slivky.web.dto;

import ru.urfu.slivky.model.UserRole;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        UserRole role,
        String fullName
) {
}
