package com.example.sqlix.repository;

import com.example.sqlix.entity.UserExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserExerciseSubmissionRepository extends JpaRepository<UserExerciseSubmission, UUID> {
    @org.springframework.data.jpa.repository.Query(value = """
            select coalesce(avg(least(100.0, greatest(0.0, s.score * 100.0 / nullif(e.max_score, 0)))), 0.0)
            from user_exercise_submissions s join practice_exercises e on e.id = s.exercise_id
            where s.user_id = :userId and s.status in ('PASSED', 'FAILED') and e.max_score > 0
            """, nativeQuery = true)
    double findAverageScoreOn100Scale(@org.springframework.data.repository.query.Param("userId") UUID userId);

    @org.springframework.data.jpa.repository.Query(value = """
            select cast(s.created_at at time zone 'Asia/Bangkok' as date) as date,
                   count(*) as "exercisesSolved",
                   coalesce(avg(least(100.0, greatest(0.0, s.score * 100.0 / nullif(e.max_score, 0)))), 0.0) as "averageScore"
            from user_exercise_submissions s join practice_exercises e on e.id = s.exercise_id
            where s.user_id = :userId and s.status in ('PASSED', 'FAILED')
              and s.created_at >= :fromDate and s.created_at < :toDate
            group by cast(s.created_at at time zone 'Asia/Bangkok' as date)
            order by date
            """, nativeQuery = true)
    java.util.List<DailySubmissionMetricsProjection> findDailyMetrics(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("fromDate") java.time.Instant fromDate,
            @org.springframework.data.repository.query.Param("toDate") java.time.Instant toDate);

    Optional<UserExerciseSubmission> findFirstByUser_IdAndExercise_IdOrderByCreatedAtDesc(
            UUID userId,
            UUID exerciseId);
}
