package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urfu.slivky.model.Animal;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
}
