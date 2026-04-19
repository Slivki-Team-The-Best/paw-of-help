package ru.urfu.slivky.web.dto;

import java.time.LocalDateTime;

public record ShelterResponse(
        Long id,
        String name,
        String description,
        String address,
        String phone,
        String email,
        String website,
        Boolean verified,
        Long createdByUserId,
        LocalDateTime createdAt
) {
}
