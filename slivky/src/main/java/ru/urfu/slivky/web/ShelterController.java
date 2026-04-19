package ru.urfu.slivky.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.urfu.slivky.service.ShelterService;
import ru.urfu.slivky.web.dto.ShelterCreateRequest;
import ru.urfu.slivky.web.dto.ShelterResponse;
import ru.urfu.slivky.web.dto.ShelterUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/shelters")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    @GetMapping("/mine")
    public List<ShelterResponse> mine() {
        return shelterService.listMine();
    }

    @GetMapping("/{id}")
    public ShelterResponse get(@PathVariable Long id) {
        return shelterService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShelterResponse create(@Valid @RequestBody ShelterCreateRequest request) {
        return shelterService.create(request);
    }

    @PatchMapping("/{id}")
    public ShelterResponse update(@PathVariable Long id, @Valid @RequestBody ShelterUpdateRequest request) {
        return shelterService.update(id, request);
    }
}
