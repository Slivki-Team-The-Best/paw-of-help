package ru.urfu.slivky.web.dto;

import java.util.List;

public record VolunteerMatchResponse(
        Long userId,
        String fullName,
        String email,
        String phone,
        Double rating,
        Integer volunteerHours,
        List<String> matchedSkills
) {
}
