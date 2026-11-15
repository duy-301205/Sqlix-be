package com.example.sqlix.repository;

import com.example.sqlix.entity.UserExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserExerciseSubmissionRepository extends JpaRepository<UserExerciseSubmission, UUID> {
    String DASHBOARD_SCORES = """
            select least(100.0, greatest(0.0, s.score * 100.0 / nullif(e.max_score, 0))) as score,
                   s.created_at as occurred_at
            from user_exercise_submissions s join practice_exercises e on e.id = s.exercise_id
            where s.user_id = :userId and s.status in ('PASSED', 'FAILED') and e.max_score > 0
            union all
            select least(100.0, greatest(0.0, s.total_score * 100.0 / nullif(t.max_score, 0))) as score,
                   s.end_time as occurred_at
            from test_sessions s join mock_tests t on t.id = s.test_id
            where s.user_id = :userId and s.status in ('COMPLETED', 'TIMEOUT')
              and s.end_time is not null and t.max_score > 0
            """;

    @org.springframework.data.jpa.repository.Query(value =
            "select coalesce(avg(score), 0.0) from (" + DASHBOARD_SCORES + ") scored_activity", nativeQuery = true)
    double findAverageScoreOn100Scale(@org.springframework.data.repository.query.Param("userId") UUID userId);

    @org.springframework.data.jpa.repository.Query(value = """
            select cast(occurred_at at time zone 'Asia/Bangkok' as date) as date,
                   count(*) as "exercisesSolved",
                   coalesce(avg(score), 0.0) as "averageScore"
            from (
            """ + DASHBOARD_SCORES + """
            ) scored_activity
            where occurred_at >= :fromDate and occurred_at < :toDate
            group by cast(occurred_at at time zone 'Asia/Bangkok' as date)
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
