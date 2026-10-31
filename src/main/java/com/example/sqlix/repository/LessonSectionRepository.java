package com.example.sqlix.repository;

import com.example.sqlix.entity.LessonSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LessonSectionRepository extends JpaRepository<LessonSection, UUID> {
}
