package ru.urfu.slivky.web.dto;

import ru.urfu.slivky.model.AnimalType;

import java.time.LocalDateTime;

public record AnimalResponse(
        Long id,
        String name,
        AnimalType type,
        String breed,
        Integer age,
        String gender,
        String description,
        String healthStatus,
        String specialNeeds,
        String[] photoUrls,
        String status,
        Long shelterId,
        Long createdById,
        LocalDateTime createdAt
) {
}
