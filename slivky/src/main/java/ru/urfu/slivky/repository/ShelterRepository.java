package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urfu.slivky.model.Shelter;

public interface ShelterRepository extends JpaRepository<Shelter, Long> {
}
