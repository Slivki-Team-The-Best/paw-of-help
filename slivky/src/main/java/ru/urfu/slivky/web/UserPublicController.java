package ru.urfu.slivky.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.slivky.service.PublicUserService;
import ru.urfu.slivky.web.dto.VolunteerPublicProfileResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPublicController {

    private final PublicUserService publicUserService;

    /**
     * Публичный профиль волонтёра (навыки, типы животных, контакты).
     */
    @GetMapping("/{id}/profile")
    public VolunteerPublicProfileResponse volunteerProfile(@PathVariable Long id) {
        return publicUserService.getVolunteerPublicProfile(id);
    }
}
