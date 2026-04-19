package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urfu.slivky.model.Shelter;

import java.util.List;

public interface ShelterRepository extends JpaRepository<Shelter, Long> {

    List<Shelter> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);
}
