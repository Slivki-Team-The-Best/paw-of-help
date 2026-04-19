package ru.urfu.slivky.web.dto;

import java.util.Map;

public record VolunteerPreferenceDto(
        Boolean worksWithCats,
        Boolean worksWithDogs,
        Boolean worksWithShelters,
        Boolean worksWithPrivate,
        Map<String, String> availabilitySchedule
) {
}
