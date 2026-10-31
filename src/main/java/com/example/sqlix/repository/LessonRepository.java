package com.example.sqlix.repository;

import com.example.sqlix.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Optional<Lesson> findBySlug(String slug);
}
