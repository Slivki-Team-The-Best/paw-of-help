package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.urfu.slivky.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByCreatedAtDesc();

    List<Task> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN FETCH t.requiredSkills
            LEFT JOIN FETCH t.animal
            LEFT JOIN FETCH t.createdBy
            LEFT JOIN FETCH t.shelter sh
            LEFT JOIN FETCH sh.createdBy
            WHERE t.id = :id
            """)
    Optional<Task> findDetailById(@Param("id") Long id);
}
