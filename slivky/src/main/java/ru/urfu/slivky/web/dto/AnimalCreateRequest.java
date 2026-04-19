package ru.urfu.slivky.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.urfu.slivky.model.AnimalType;

import java.util.List;

public record AnimalCreateRequest(
        @Size(max = 100) String name,
        @NotNull AnimalType type,
        @Size(max = 100) String breed,
        Integer age,
        @Size(max = 10) String gender,
        String description,
        String healthStatus,
        String specialNeeds,
        List<String> photoUrls,
        Long shelterId,
        @Size(max = 50) String status
) {
}
