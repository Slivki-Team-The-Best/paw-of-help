package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.exception.BadRequestException;
import ru.urfu.slivky.model.User;
import ru.urfu.slivky.model.UserRole;
import ru.urfu.slivky.model.VolunteerPreference;
import ru.urfu.slivky.repository.SkillRepository;
import ru.urfu.slivky.repository.UserRepository;
import ru.urfu.slivky.security.CurrentUser;
import ru.urfu.slivky.web.dto.ProfileResponse;
import ru.urfu.slivky.web.dto.ProfileUpdateRequest;
import ru.urfu.slivky.web.dto.SkillDto;
import ru.urfu.slivky.web.dto.VolunteerPreferenceDto;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMe() {
        User tokenUser = CurrentUser.get();
        User user = userRepository.findProfileById(tokenUser.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(ProfileUpdateRequest req) {
        User tokenUser = CurrentUser.get();
        User user = userRepository.findProfileById(tokenUser.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName().trim());
        }
        if (req.phone() != null) {
            user.setPhone(req.phone().isBlank() ? null : req.phone().trim());
        }

        if (user.getRole() == UserRole.VOLUNTEER && req.skillIds() != null) {
            var skills = skillRepository.findAllById(req.skillIds());
            if (skills.size() != req.skillIds().size()) {
                throw new BadRequestException("Invalid skill ids");
            }
            user.getSkills().clear();
            user.getSkills().addAll(new HashSet<>(skills));
        }

        if (user.getRole() == UserRole.VOLUNTEER && req.preferences() != null) {
            VolunteerPreference pref = user.getVolunteerPreference();
            if (pref == null) {
                pref = new VolunteerPreference();
                pref.setUser(user);
                user.setVolunteerPreference(pref);
            }
            VolunteerPreferenceDto p = req.preferences();
            if (p.worksWithCats() != null) {
                pref.setWorksWithCats(p.worksWithCats());
            }
            if (p.worksWithDogs() != null) {
                pref.setWorksWithDogs(p.worksWithDogs());
            }
            if (p.worksWithShelters() != null) {
                pref.setWorksWithShelters(p.worksWithShelters());
            }
            if (p.worksWithPrivate() != null) {
                pref.setWorksWithPrivate(p.worksWithPrivate());
            }
            if (p.availabilitySchedule() != null) {
                pref.setAvailabilitySchedule(p.availabilitySchedule());
            }
        }

        return toResponse(user);
    }

    private ProfileResponse toResponse(User user) {
        List<SkillDto> skills = user.getSkills().stream()
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

        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getRating(),
                user.getVolunteerHours(),
                skills,
                prefDto
        );
    }
}
