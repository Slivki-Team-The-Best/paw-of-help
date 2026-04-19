package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.exception.NotFoundException;
import ru.urfu.slivky.model.User;
import ru.urfu.slivky.model.UserRole;
import ru.urfu.slivky.model.VolunteerPreference;
import ru.urfu.slivky.repository.UserRepository;
import ru.urfu.slivky.web.dto.SkillDto;
import ru.urfu.slivky.web.dto.VolunteerPreferenceDto;
import ru.urfu.slivky.web.dto.VolunteerPublicProfileResponse;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public VolunteerPublicProfileResponse getVolunteerPublicProfile(Long userId) {
        User user = userRepository.findProfileById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() != UserRole.VOLUNTEER) {
            throw new NotFoundException("User not found");
        }

        var skills = user.getSkills().stream()
                .map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory()))
                .collect(Collectors.toList());

        VolunteerPreferenceDto prefDto = null;
        VolunteerPreference p = user.getVolunteerPreference();
        if (p != null) {
            prefDto = new VolunteerPreferenceDto(
                    p.getWorksWithCats(),
                    p.getWorksWithDogs(),
                    p.getWorksWithShelters(),
                    p.getWorksWithPrivate(),
                    p.getAvailabilitySchedule()
            );
        }

        return new VolunteerPublicProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRating(),
                user.getVolunteerHours(),
                skills,
                prefDto
        );
    }
}
