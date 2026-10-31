package com.example.sqlix.repository;

import com.example.sqlix.entity.UserDailyMetric;
import com.example.sqlix.entity.UserDailyMetricId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDailyMetricRepository extends JpaRepository<UserDailyMetric, UserDailyMetricId> {
    @Modifying
    @Query(value = """
            insert into user_daily_metrics (user_id, record_date, study_time_seconds, lessons_completed, exercises_solved, total_score)
            values (:userId, current_date, :seconds, 0, 0, 0)
            on conflict (user_id, record_date) do update
            set study_time_seconds = user_daily_metrics.study_time_seconds + excluded.study_time_seconds
            """, nativeQuery = true)
    int addStudyTime(@Param("userId") UUID userId, @Param("seconds") int seconds);

    @Modifying
    @Query(value = """
            insert into user_daily_metrics (user_id, record_date, study_time_seconds, lessons_completed, exercises_solved, total_score)
            values (:userId, current_date, 0, 1, 0, 0)
            on conflict (user_id, record_date) do update
            set lessons_completed = user_daily_metrics.lessons_completed + 1
            """, nativeQuery = true)
    int incrementLessonsCompleted(@Param("userId") UUID userId);

    @Modifying
    @Query(value = """
            insert into user_daily_metrics (user_id, record_date, study_time_seconds, lessons_completed, exercises_solved, total_score)
            values (:userId, current_date, 0, 0, 1, :score)
            on conflict (user_id, record_date) do update
            set exercises_solved = user_daily_metrics.exercises_solved + 1,
                total_score = user_daily_metrics.total_score + excluded.total_score
            """, nativeQuery = true)
    int incrementExercisesSolved(@Param("userId") UUID userId, @Param("score") int score);

    @Query(value = "select current_date", nativeQuery = true)
    LocalDate getCurrentDatabaseDate();

    @Query("""
            select coalesce(sum(m.studyTimeSeconds), 0) as totalStudyTimeSeconds,
                   coalesce(sum(m.lessonsCompleted), 0) as totalLessonsCompleted,
                   coalesce(sum(m.exercisesSolved), 0) as totalExercisesSolved,
                   coalesce(avg(m.totalScore), 0.0) as averageScore
            from UserDailyMetric m
            where m.user.id = :userId
            """)
    DashboardMetricsProjection getOverviewMetrics(@Param("userId") UUID userId);

    @Query("""
            select coalesce(m.studyTimeSeconds, 0)
            from UserDailyMetric m
            where m.id.userId = :userId and m.id.recordDate = current_date
            """)
    Optional<Integer> findTodayStudyTimeSeconds(@Param("userId") UUID userId);

    @Query("""
            select m from UserDailyMetric m
            where m.user.id = :userId
              and m.id.recordDate between :startDate and :endDate
            order by m.id.recordDate desc
            """)
    List<UserDailyMetric> findLast30Days(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
