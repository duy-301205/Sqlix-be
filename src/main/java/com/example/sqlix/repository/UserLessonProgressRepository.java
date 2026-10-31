package com.example.sqlix.repository;

import com.example.sqlix.entity.UserLessonProgress;
import com.example.sqlix.entity.UserLessonProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UserLessonProgressId> {
    @Query("""
            select progress from UserLessonProgress progress
            where progress.id.userId = :userId and progress.id.lessonId = :lessonId
            """)
    Optional<UserLessonProgress> findByUserIdAndLessonId(
            @Param("userId") UUID userId,
            @Param("lessonId") UUID lessonId);

    @Query("select progress from UserLessonProgress progress where progress.id.userId = :userId")
    List<UserLessonProgress> findAllByUserId(@Param("userId") UUID userId);
}
