package ru.urfu.slivky.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.urfu.slivky.service.AnimalService;
import ru.urfu.slivky.web.dto.AnimalCreateRequest;
import ru.urfu.slivky.web.dto.AnimalResponse;

import java.util.List;

@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping
    public List<AnimalResponse> list(@RequestParam(required = false) Long shelterId) {
        return animalService.list(shelterId);
    }

    @GetMapping("/{id}")
    public AnimalResponse get(@PathVariable Long id) {
        return animalService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalResponse create(@Valid @RequestBody AnimalCreateRequest request) {
        return animalService.create(request);
    }
}
