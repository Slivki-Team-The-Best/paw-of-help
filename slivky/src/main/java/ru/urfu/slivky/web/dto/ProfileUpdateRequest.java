package ru.urfu.slivky.web.dto;

import java.util.List;

public record ProfileUpdateRequest(
        String fullName,
        String phone,
        List<Long> skillIds,
        VolunteerPreferenceDto preferences
) {
}
