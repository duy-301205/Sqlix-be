package com.example.sqlix.repository;

import com.example.sqlix.entity.UserDailyMetric;
import com.example.sqlix.entity.UserDailyMetricId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDailyMetricRepository extends JpaRepository<UserDailyMetric, UserDailyMetricId> {
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
