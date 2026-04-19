package ru.urfu.slivky.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.slivky.repository.SkillRepository;
import ru.urfu.slivky.web.dto.SkillDto;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillsController {

    private final SkillRepository skillRepository;

    @GetMapping
    public List<SkillDto> list() {
        return skillRepository.findAll().stream()
                .map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory()))
                .toList();
    }
}
