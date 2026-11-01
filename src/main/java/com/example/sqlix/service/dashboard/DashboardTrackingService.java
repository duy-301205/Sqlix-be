package com.example.sqlix.service.dashboard;

import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserProfile;
import com.example.sqlix.entity.UserRecentActivity;
import com.example.sqlix.entity.UserTopicMastery;
import com.example.sqlix.entity.UserTopicMasteryId;
import com.example.sqlix.enums.ActivityType;
import com.example.sqlix.enums.SqlTopicCode;
import com.example.sqlix.repository.UserDailyMetricRepository;
import com.example.sqlix.repository.UserProfileRepository;
import com.example.sqlix.repository.UserRecentActivityRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.repository.UserTopicMasteryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardTrackingService {
    private static final BigDecimal MASTERY_INCREMENT = new BigDecimal("5.00");
    private static final BigDecimal MAX_MASTERY = new BigDecimal("100.00");

    private final UserDailyMetricRepository dailyMetricRepository;
    private final UserTopicMasteryRepository topicMasteryRepository;
    private final UserRecentActivityRepository recentActivityRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logStudyTime(UUID userId, int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Study time cannot be negative");
        }

        dailyMetricRepository.addStudyTime(userId, seconds);

        int accumulatedSeconds = dailyMetricRepository.findTodayStudyTimeSeconds(userId).orElse(0);
        LocalDate today = dailyMetricRepository.getCurrentDatabaseDate();
        userProfileRepository.findByIdForUpdate(userId).ifPresent(profile ->
                updateStreakIfDailyTargetReached(profile, accumulatedSeconds, today));
    }

    @Transactional
    public void logLessonCompleted(UUID userId, UUID lessonId, String lessonTitle) {
        dailyMetricRepository.incrementLessonsCompleted(userId);

        UserRecentActivity activity = new UserRecentActivity();
        activity.setUser(userRepository.getReferenceById(userId));
        activity.setType(ActivityType.LESSON_COMPLETED);
        activity.setReferenceId(lessonId);
        activity.setMetaData(Map.of("title", lessonTitle));
        recentActivityRepository.save(activity);
    }

    @Transactional
    public void logExerciseSolved(
            UUID userId,
            SqlTopicCode topicCode,
            UUID exerciseId,
            String exerciseTitle,
            int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Exercise score cannot be negative");
        }
        dailyMetricRepository.incrementExercisesSolved(userId, score);
        if (topicCode != null) {
            incrementTopicMastery(userId, topicCode);
        }

        UserRecentActivity activity = new UserRecentActivity();
        activity.setUser(userRepository.getReferenceById(userId));
        activity.setType(ActivityType.EXERCISE_SOLVED);
        activity.setReferenceId(exerciseId);
        activity.setMetaData(Map.of("title", exerciseTitle, "score", score));
        recentActivityRepository.save(activity);
    }

    private void incrementTopicMastery(UUID userId, SqlTopicCode topicCode) {
        UserTopicMasteryId id = new UserTopicMasteryId(userId, topicCode);
        UserTopicMastery mastery = topicMasteryRepository.findByIdForUpdate(id)
                .orElseGet(() -> {
                    UserTopicMastery newMastery = new UserTopicMastery();
                    newMastery.setId(id);
                    newMastery.setUser(userRepository.getReferenceById(userId));
                    newMastery.setMasteryPercentage(BigDecimal.ZERO.setScale(2));
                    newMastery.setExercisesSolvedInTopic(0);
                    return newMastery;
                });

        BigDecimal currentPercentage = mastery.getMasteryPercentage() == null
                ? BigDecimal.ZERO
                : mastery.getMasteryPercentage();
        mastery.setMasteryPercentage(currentPercentage.add(MASTERY_INCREMENT).min(MAX_MASTERY));
        mastery.setExercisesSolvedInTopic(mastery.getExercisesSolvedInTopic() == null
                ? 1
                : mastery.getExercisesSolvedInTopic() + 1);
        topicMasteryRepository.save(mastery);
    }

    private void updateStreakIfDailyTargetReached(UserProfile profile, int accumulatedSeconds, LocalDate today) {
        int targetMinutes = profile.getDailyTargetMinutes() == null ? 0 : profile.getDailyTargetMinutes();
        if (accumulatedSeconds < targetMinutes * 60L || today.equals(profile.getLastActiveDate())) {
            return;
        }

        int previousStreak = profile.getCurrentStreak() == null ? 0 : profile.getCurrentStreak();
        int newStreak = today.minusDays(1).equals(profile.getLastActiveDate()) ? previousStreak + 1 : 1;
        int longestStreak = profile.getLongestStreak() == null ? 0 : profile.getLongestStreak();

        profile.setCurrentStreak(newStreak);
        profile.setLongestStreak(Math.max(longestStreak, newStreak));
        profile.setLastActiveDate(today);
    }
}
