package ru.urfu.slivky.web.dto;

import java.util.List;

/**
 * Публичный профиль волонтёра (контакты и навыки для подбора и координации).
 */
public record VolunteerPublicProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Double rating,
        Integer volunteerHours,
        List<SkillDto> skills,
        VolunteerPreferenceDto preferences
) {
}
