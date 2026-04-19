package ru.urfu.slivky.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.urfu.slivky.service.ProfileService;
import ru.urfu.slivky.web.dto.ProfileResponse;
import ru.urfu.slivky.web.dto.ProfileUpdateRequest;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileResponse me() {
        return profileService.getMe();
    }

    @PatchMapping
    public ProfileResponse update(@Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.updateProfile(request);
    }
}
