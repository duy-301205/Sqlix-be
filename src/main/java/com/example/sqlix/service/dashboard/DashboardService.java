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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserDailyMetricRepository dailyMetricRepository;
    private final UserTopicMasteryRepository topicMasteryRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRecentActivityRepository recentActivityRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(UUID userId) {
        DashboardMetricsProjection metrics = dailyMetricRepository.getOverviewMetrics(userId);
        int todayStudyTime = dailyMetricRepository.findTodayStudyTimeSeconds(userId).orElse(0);
        int dailyTargetMinutes = userProfileRepository.findById(userId)
                .map(UserProfile::getDailyTargetMinutes)
                .orElse(0);

        return new DashboardOverviewResponse(
                metrics == null || metrics.getTotalStudyTimeSeconds() == null ? 0L : metrics.getTotalStudyTimeSeconds(),
                metrics == null || metrics.getTotalLessonsCompleted() == null ? 0L : metrics.getTotalLessonsCompleted(),
                metrics == null || metrics.getTotalExercisesSolved() == null ? 0L : metrics.getTotalExercisesSolved(),
                metrics == null || metrics.getAverageScore() == null ? 0.0 : metrics.getAverageScore(),
                todayStudyTime,
                dailyTargetMinutes);
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
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);
        return dailyMetricRepository.findLast30Days(userId, startDate, today).stream()
                .map(metric -> {
                    int exercisesSolved = metric.getExercisesSolved() == null ? 0 : metric.getExercisesSolved();
                    double averageScore = exercisesSolved == 0 || metric.getTotalScore() == null
                            ? 0.0
                            : (double) metric.getTotalScore() / exercisesSolved;
                    return new DailyChartResponse(metric.getId().getRecordDate(), exercisesSolved, averageScore);
                })
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
