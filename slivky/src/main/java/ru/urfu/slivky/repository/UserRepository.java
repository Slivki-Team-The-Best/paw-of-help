package ru.urfu.slivky.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.urfu.slivky.model.User;
import ru.urfu.slivky.model.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.skills
            LEFT JOIN FETCH u.volunteerPreference
            WHERE u.role = :role
            """)
    List<User> findVolunteersWithProfiles(@Param("role") UserRole role);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.skills
            LEFT JOIN FETCH u.volunteerPreference
            WHERE u.id = :id
            """)
    Optional<User> findProfileById(@Param("id") Long id);
}
