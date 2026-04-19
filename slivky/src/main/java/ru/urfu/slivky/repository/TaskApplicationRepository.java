package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urfu.slivky.model.TaskApplication;

import java.util.List;

public interface TaskApplicationRepository extends JpaRepository<TaskApplication, Long> {

    List<TaskApplication> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    List<TaskApplication> findByVolunteer_IdOrderByCreatedAtDesc(Long volunteerId);

    boolean existsByTaskIdAndVolunteerId(Long taskId, Long volunteerId);
}
