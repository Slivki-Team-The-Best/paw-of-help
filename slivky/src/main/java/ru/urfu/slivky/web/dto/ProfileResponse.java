package ru.urfu.slivky.web.dto;

import ru.urfu.slivky.model.UserRole;

import java.util.List;

public record ProfileResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        UserRole role,
        Double rating,
        Integer volunteerHours,
        List<SkillDto> skills,
        VolunteerPreferenceDto preferences
) {
}
