package com.example.sqlix.repository;

import com.example.sqlix.entity.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    @EntityGraph(attributePaths = "sections")
    Optional<Lesson> findBySlug(String slug);
}
