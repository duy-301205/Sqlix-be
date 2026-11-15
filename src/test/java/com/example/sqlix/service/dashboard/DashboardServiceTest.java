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
    @Mock com.example.sqlix.repository.LessonRepository lessonRepository;
    @Mock com.example.sqlix.repository.PracticeExerciseRepository exerciseRepository;
    @Mock com.example.sqlix.repository.UserLessonProgressRepository lessonProgressRepository;
    @Mock com.example.sqlix.repository.UserExerciseProgressRepository exerciseProgressRepository;
    @Mock com.example.sqlix.repository.UserExerciseSubmissionRepository submissionRepository;
    @Mock com.example.sqlix.repository.TestSessionRepository sessionRepository;
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
        when(lessonProgressRepository.countByUser_IdAndStatus(userId, com.example.sqlix.enums.LessonStatus.COMPLETED)).thenReturn(3L);
        when(lessonRepository.count()).thenReturn(8L);
        when(exerciseRepository.count()).thenReturn(24L);
        when(exerciseProgressRepository.countByUser_IdAndIsPassedTrue(userId)).thenReturn(12L);
        when(submissionRepository.findAverageScoreOn100Scale(userId)).thenReturn(84.5);
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
        assertThat(response.totalLessons()).isEqualTo(8);
        assertThat(response.totalExercises()).isEqualTo(24);
        assertThat(response.recentMockTest()).isNull();
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
    void chartFillsMissingDaysAndUsesNormalizedSubmissionScores() {
        UUID userId = UUID.randomUUID();
        var zone = java.time.ZoneId.of("Asia/Bangkok");
        LocalDate today = LocalDate.now(zone);
        var metric = mock(com.example.sqlix.repository.DailySubmissionMetricsProjection.class);
        when(metric.getDate()).thenReturn(today.minusDays(10));
        when(metric.getExercisesSolved()).thenReturn(4);
        when(metric.getAverageScore()).thenReturn(75.0);
        when(submissionRepository.findDailyMetrics(userId,
                today.minusDays(29).atStartOfDay(zone).toInstant(),
                today.plusDays(1).atStartOfDay(zone).toInstant())).thenReturn(List.of(metric));
        var chart = dashboardService.get30DayActivityChart(userId);
        assertThat(chart).hasSize(30);
        assertThat(chart.get(19)).isEqualTo(new com.example.sqlix.dto.response.DailyChartResponse(today.minusDays(10), 4, 75.0));
        assertThat(chart.getFirst().date()).isEqualTo(today.minusDays(29));
        assertThat(chart.getLast()).isEqualTo(new com.example.sqlix.dto.response.DailyChartResponse(today, 0, 0.0));
    }

    @Test
    void chartWithoutSubmissionsHasNoInventedActivity() {
        var chart = dashboardService.get30DayActivityChart(UUID.randomUUID());
        assertThat(chart).hasSize(30).allSatisfy(day -> {
            assertThat(day.exercisesSolved()).isZero();
            assertThat(day.averageScore()).isZero();
        });
    }

    @Test
    void recentExamUsesFinishedSessionAndConvertsScoreTo100() {
        UUID userId = UUID.randomUUID();
        var test = new com.example.sqlix.entity.MockTest();
        test.setTitle("Real SQL exam"); test.setMaxScore(10); test.setTotalQuestions(12);
        var session = new com.example.sqlix.entity.TestSession();
        session.setId(UUID.randomUUID()); session.setTest(test); session.setTotalScore(8);
        session.setDurationSeconds(125); session.setIsPassed(true);
        session.setEndTime(Instant.parse("2026-11-15T10:00:00Z"));
        when(sessionRepository.findFirstByUser_IdAndStatusInOrderByEndTimeDesc(userId,
                List.of(com.example.sqlix.enums.TestSessionStatus.COMPLETED, com.example.sqlix.enums.TestSessionStatus.TIMEOUT)))
                .thenReturn(Optional.of(session));
        var recent = dashboardService.getOverview(userId).recentMockTest();
        assertThat(recent.sessionId()).isEqualTo(session.getId());
        assertThat(recent.title()).isEqualTo("Real SQL exam");
        assertThat(recent.score()).isEqualTo(80);
        assertThat(recent.durationSeconds()).isEqualTo(125);
        assertThat(recent.questionCount()).isEqualTo(12);
        assertThat(recent.submittedAt()).isEqualTo(session.getEndTime());
        assertThat(recent.isPassed()).isTrue();
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
