package com.example.sqlix.service.dashboard;

import com.example.sqlix.dto.response.DashboardOverviewResponse;
import com.example.sqlix.dto.response.DailyChartResponse;
import com.example.sqlix.dto.response.RecentActivityResponse;
import com.example.sqlix.dto.response.TopicMasteryResponse;
import com.example.sqlix.entity.UserProfile;
import com.example.sqlix.repository.DashboardMetricsProjection;
import com.example.sqlix.repository.UserDailyMetricRepository;
import com.example.sqlix.repository.UserProfileRepository;
import com.example.sqlix.repository.UserRecentActivityRepository;
import com.example.sqlix.repository.UserTopicMasteryRepository;
import com.example.sqlix.repository.LessonRepository;
import com.example.sqlix.repository.PracticeExerciseRepository;
import com.example.sqlix.repository.UserLessonProgressRepository;
import com.example.sqlix.repository.UserExerciseProgressRepository;
import com.example.sqlix.repository.UserExerciseSubmissionRepository;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.dto.response.RecentMockTestResponse;
import com.example.sqlix.enums.LessonStatus;
import com.example.sqlix.enums.TestSessionStatus;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserDailyMetricRepository dailyMetricRepository;
    private final UserTopicMasteryRepository topicMasteryRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRecentActivityRepository recentActivityRepository;
    private final LessonRepository lessonRepository;
    private final PracticeExerciseRepository exerciseRepository;
    private final UserLessonProgressRepository lessonProgressRepository;
    private final UserExerciseProgressRepository exerciseProgressRepository;
    private final UserExerciseSubmissionRepository submissionRepository;
    private final TestSessionRepository sessionRepository;
    private static final ZoneId ACTIVITY_ZONE = ZoneId.of("Asia/Bangkok");

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(UUID userId) {
        DashboardMetricsProjection metrics = dailyMetricRepository.getOverviewMetrics(userId);
        int todayStudyTime = dailyMetricRepository.findTodayStudyTimeSeconds(userId).orElse(0);
        int dailyTargetMinutes = userProfileRepository.findById(userId)
                .map(UserProfile::getDailyTargetMinutes)
                .orElse(0);

        return new DashboardOverviewResponse(
                metrics == null || metrics.getTotalStudyTimeSeconds() == null ? 0L : metrics.getTotalStudyTimeSeconds(),
                lessonProgressRepository.countByUser_IdAndStatus(userId, LessonStatus.COMPLETED),
                exerciseProgressRepository.countByUser_IdAndIsPassedTrue(userId),
                submissionRepository.findAverageScoreOn100Scale(userId),
                todayStudyTime,
                dailyTargetMinutes,
                lessonRepository.count(),
                exerciseRepository.count(),
                getRecentMockTest(userId));
    }

    private RecentMockTestResponse getRecentMockTest(UUID userId) {
        return sessionRepository.findFirstByUser_IdAndStatusInOrderByEndTimeDesc(userId,
                List.of(TestSessionStatus.COMPLETED, TestSessionStatus.TIMEOUT)).map(session -> {
            var test = session.getTest();
            int maxScore = test.getMaxScore() == null ? 0 : test.getMaxScore();
            double score = maxScore <= 0 || session.getTotalScore() == null ? 0.0
                    : Math.max(0, Math.min(100, session.getTotalScore() * 100.0 / maxScore));
            return new RecentMockTestResponse(session.getId(), test.getTitle(), session.getEndTime(), score,
                    session.getDurationSeconds() == null ? 0 : session.getDurationSeconds(),
                    test.getTotalQuestions() == null ? 0 : test.getTotalQuestions(),
                    Boolean.TRUE.equals(session.getIsPassed()));
        }).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TopicMasteryResponse> getTopicMasteries(UUID userId) {
        return topicMasteryRepository.findAllByUserId(userId).stream()
                .map(mastery -> new TopicMasteryResponse(
                        mastery.getId().getTopicCode(), mastery.getMasteryPercentage()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DailyChartResponse> get30DayActivityChart(UUID userId) {
        LocalDate today = LocalDate.now(ACTIVITY_ZONE);
        LocalDate startDate = today.minusDays(29);
        Map<LocalDate, DailyChartResponse> metricsByDate = submissionRepository
                .findDailyMetrics(userId, startDate.atStartOfDay(ACTIVITY_ZONE).toInstant(),
                        today.plusDays(1).atStartOfDay(ACTIVITY_ZONE).toInstant()).stream()
                .map(metric -> {
                    int exercisesSolved = metric.getExercisesSolved() == null ? 0 : metric.getExercisesSolved();
                    double averageScore = metric.getAverageScore() == null ? 0.0 : metric.getAverageScore();
                    return new DailyChartResponse(metric.getDate(), exercisesSolved, averageScore);
                })
                .collect(Collectors.toMap(DailyChartResponse::date, Function.identity()));

        return IntStream.range(0, 30)
                .mapToObj(offset -> startDate.plusDays(offset))
                .map(date -> metricsByDate.getOrDefault(date, new DailyChartResponse(date, 0, 0.0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecentActivityResponse> getRecentActivities(UUID userId) {
        return recentActivityRepository.findTop5ByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(activity -> new RecentActivityResponse(
                        activity.getId(),
                        activity.getType(),
                        activity.getReferenceId(),
                        activity.getMetaData(),
                        activity.getCreatedAt()))
                .toList();
    }
}
