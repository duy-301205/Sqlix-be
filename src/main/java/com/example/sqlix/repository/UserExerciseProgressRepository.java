package com.example.sqlix.repository;

import com.example.sqlix.entity.UserExerciseProgress;
import com.example.sqlix.entity.UserExerciseProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserExerciseProgressRepository extends JpaRepository<UserExerciseProgress, UserExerciseProgressId> {
    @Query("""
            select progress from UserExerciseProgress progress
            where progress.id.userId = :userId and progress.id.exerciseId in :exerciseIds
            """)
    List<UserExerciseProgress> findAllForUserAndExercises(
            @Param("userId") UUID userId,
            @Param("exerciseIds") Collection<UUID> exerciseIds);
}
