package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urfu.slivky.model.Animal;

import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    List<Animal> findAllByOrderByCreatedAtDesc();

    List<Animal> findByShelter_IdOrderByCreatedAtDesc(Long shelterId);
}
