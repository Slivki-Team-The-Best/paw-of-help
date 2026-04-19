package ru.urfu.slivky.web.dto;

import jakarta.validation.constraints.Size;

public record ShelterUpdateRequest(
        @Size(max = 255) String name,
        String description,
        String address,
        String phone,
        String email,
        String website
) {
}
