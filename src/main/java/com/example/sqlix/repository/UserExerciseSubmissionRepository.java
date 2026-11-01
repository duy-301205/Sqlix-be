package com.example.sqlix.repository;

import com.example.sqlix.entity.UserExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserExerciseSubmissionRepository extends JpaRepository<UserExerciseSubmission, UUID> {
    Optional<UserExerciseSubmission> findFirstByUser_IdAndExercise_IdOrderByCreatedAtDesc(
            UUID userId,
            UUID exerciseId);
}
