package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urfu.slivky.model.Skill;

import java.util.Collection;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findAllByIdIn(Collection<Long> ids);
}
