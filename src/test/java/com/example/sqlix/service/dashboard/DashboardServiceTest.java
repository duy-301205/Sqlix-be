package com.example.sqlix.service.dashboard;

import com.example.sqlix.entity.UserTopicMastery;
import com.example.sqlix.entity.UserTopicMasteryId;
import com.example.sqlix.entity.UserDailyMetric;
import com.example.sqlix.entity.UserDailyMetricId;
import com.example.sqlix.entity.UserRecentActivity;
import com.example.sqlix.enums.SqlTopicCode;
import com.example.sqlix.repository.DashboardMetricsProjection;
import com.example.sqlix.repository.UserDailyMetricRepository;
import com.example.sqlix.repository.UserProfileRepository;
import com.example.sqlix.repository.UserRecentActivityRepository;
import com.example.sqlix.repository.UserTopicMasteryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock UserDailyMetricRepository dailyMetricRepository;
    @Mock UserTopicMasteryRepository topicMasteryRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock UserRecentActivityRepository recentActivityRepository;
    @InjectMocks DashboardService dashboardService;

    @Test
    void getOverviewReturnsZeroMetricsForNewUserWithoutRecords() {
        UUID userId = UUID.randomUUID();
        when(dailyMetricRepository.getOverviewMetrics(userId)).thenReturn(null);
        when(dailyMetricRepository.findTodayStudyTimeSeconds(userId)).thenReturn(Optional.empty());
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        var response = dashboardService.getOverview(userId);

        assertThat(response.totalStudyTimeSeconds()).isZero();
        assertThat(response.totalLessonsCompleted()).isZero();
        assertThat(response.totalExercisesSolved()).isZero();
        assertThat(response.averageScore()).isZero();
        assertThat(response.todayStudyTimeSeconds()).isZero();
        assertThat(response.dailyTargetMinutes()).isZero();
    }

    @Test
    void getOverviewReturnsAggregatesAndProfileTarget() {
        UUID userId = UUID.randomUUID();
        DashboardMetricsProjection metrics = mock(DashboardMetricsProjection.class);
        when(metrics.getTotalStudyTimeSeconds()).thenReturn(900L);
        when(metrics.getTotalLessonsCompleted()).thenReturn(3L);
        when(metrics.getTotalExercisesSolved()).thenReturn(12L);
        when(metrics.getAverageScore()).thenReturn(84.5);
        when(dailyMetricRepository.getOverviewMetrics(userId)).thenReturn(metrics);
        when(dailyMetricRepository.findTodayStudyTimeSeconds(userId)).thenReturn(Optional.of(300));
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(
                com.example.sqlix.entity.UserProfile.builder().dailyTargetMinutes(25).build()));

        var response = dashboardService.getOverview(userId);

        assertThat(response.totalStudyTimeSeconds()).isEqualTo(900L);
        assertThat(response.totalLessonsCompleted()).isEqualTo(3L);
        assertThat(response.totalExercisesSolved()).isEqualTo(12L);
        assertThat(response.averageScore()).isEqualTo(84.5);
        assertThat(response.todayStudyTimeSeconds()).isEqualTo(300);
        assertThat(response.dailyTargetMinutes()).isEqualTo(25);
    }

    @Test
    void getTopicMasteriesMapsEntitiesToResponses() {
        UUID userId = UUID.randomUUID();
        UserTopicMastery mastery = new UserTopicMastery();
        mastery.setId(new UserTopicMasteryId(userId, SqlTopicCode.JOINS));
        mastery.setMasteryPercentage(new BigDecimal("82.50"));
        when(topicMasteryRepository.findAllByUserId(userId)).thenReturn(List.of(mastery));

        var responses = dashboardService.getTopicMasteries(userId);

        assertThat(responses).containsExactly(new com.example.sqlix.dto.response.TopicMasteryResponse(
                SqlTopicCode.JOINS, new BigDecimal("82.50")));
    }

    @Test
    void get30DayActivityChartReturnsZeroAverageWhenNoExercisesWereSolved() {
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        UserDailyMetric metric = new UserDailyMetric();
        metric.setId(new UserDailyMetricId(userId, today));
        metric.setExercisesSolved(0);
        metric.setTotalScore(100);
        when(dailyMetricRepository.findLast30Days(userId, today.minusDays(29), today))
                .thenReturn(List.of(metric));

        var chart = dashboardService.get30DayActivityChart(userId);

        assertThat(chart).containsExactly(new com.example.sqlix.dto.response.DailyChartResponse(today, 0, 0.0));
    }

    @Test
    void get30DayActivityChartCalculatesAverageScorePerExercise() {
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        UserDailyMetric metric = new UserDailyMetric();
        metric.setId(new UserDailyMetricId(userId, today));
        metric.setExercisesSolved(4);
        metric.setTotalScore(300);
        when(dailyMetricRepository.findLast30Days(userId, today.minusDays(29), today))
                .thenReturn(List.of(metric));

        var chart = dashboardService.get30DayActivityChart(userId);

        assertThat(chart.getFirst().averageScore()).isEqualTo(75.0);
    }

    @Test
    void getRecentActivitiesMapsLatestActivityFields() {
        UUID userId = UUID.randomUUID();
        UserRecentActivity activity = new UserRecentActivity();
        UUID activityId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-10-31T10:00:00Z");
        Map<String, Object> metadata = Map.of("title", "Join practice", "score", 90);
        activity.setId(activityId);
        activity.setType(com.example.sqlix.enums.ActivityType.EXERCISE_SOLVED);
        activity.setReferenceId(referenceId);
        activity.setMetaData(metadata);
        activity.setCreatedAt(createdAt);
        when(recentActivityRepository.findTop5ByUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(activity));

        var responses = dashboardService.getRecentActivities(userId);

        assertThat(responses).containsExactly(new com.example.sqlix.dto.response.RecentActivityResponse(
                activityId, com.example.sqlix.enums.ActivityType.EXERCISE_SOLVED, referenceId, metadata, createdAt));
    }
}
