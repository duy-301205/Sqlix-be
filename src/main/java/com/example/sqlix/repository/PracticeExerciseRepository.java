package com.example.sqlix.repository;

import com.example.sqlix.entity.PracticeExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PracticeExerciseRepository extends JpaRepository<PracticeExercise, UUID> {
    List<PracticeExercise> findAllByDatabase_IdOrderBySortOrderAsc(UUID databaseId);
}
