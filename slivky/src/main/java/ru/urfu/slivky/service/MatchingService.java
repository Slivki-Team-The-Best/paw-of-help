package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.model.*;
import ru.urfu.slivky.repository.UserRepository;
import ru.urfu.slivky.web.dto.VolunteerMatchResponse;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Упрощённый мэтчинг (MVP): пересечение навыков, фильтр по типу животного из задачи,
 * сортировка по рейтингу волонтёра. Без ML и чата.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<VolunteerMatchResponse> matchVolunteers(Task task) {
        List<User> volunteers = userRepository.findVolunteersWithProfiles(UserRole.VOLUNTEER);
        Set<Long> requiredSkillIds = task.getRequiredSkills().stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());

        AnimalType animalType = task.getAnimal() != null ? task.getAnimal().getType() : null;

        return volunteers.stream()
                .filter(v -> passesAnimalPreference(v, animalType))
                .filter(v -> passesSkills(v, requiredSkillIds))
                .sorted(Comparator.comparing(User::getRating, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(50)
                .map(v -> toMatch(v, requiredSkillIds))
                .toList();
    }

    private boolean passesSkills(User volunteer, Set<Long> requiredSkillIds) {
        if (requiredSkillIds.isEmpty()) {
            return true;
        }
        Set<Long> mine = volunteer.getSkills().stream().map(Skill::getId).collect(Collectors.toSet());
        return mine.stream().anyMatch(requiredSkillIds::contains);
    }

    private boolean passesAnimalPreference(User volunteer, AnimalType animalType) {
        if (animalType == null || animalType == AnimalType.OTHER) {
            return true;
        }
        VolunteerPreference p = volunteer.getVolunteerPreference();
        if (p == null) {
            return true;
        }
        if (animalType == AnimalType.CAT) {
            return Boolean.TRUE.equals(p.getWorksWithCats());
        }
        if (animalType == AnimalType.DOG) {
            return Boolean.TRUE.equals(p.getWorksWithDogs());
        }
        return true;
    }

    private VolunteerMatchResponse toMatch(User volunteer, Set<Long> requiredSkillIds) {
        List<String> matchedNames = volunteer.getSkills().stream()
                .filter(s -> requiredSkillIds.contains(s.getId()))
                .map(Skill::getName)
                .sorted()
                .toList();
        return new VolunteerMatchResponse(
                volunteer.getId(),
                volunteer.getFullName(),
                volunteer.getEmail(),
                volunteer.getPhone(),
                volunteer.getRating(),
                volunteer.getVolunteerHours(),
                matchedNames
        );
    }
}
