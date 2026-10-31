package com.example.sqlix.service.dashboard;

import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserProfile;
import com.example.sqlix.entity.UserTopicMastery;
import com.example.sqlix.entity.UserTopicMasteryId;
import com.example.sqlix.enums.SqlTopicCode;
import com.example.sqlix.repository.UserDailyMetricRepository;
import com.example.sqlix.repository.UserProfileRepository;
import com.example.sqlix.repository.UserRecentActivityRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.repository.UserTopicMasteryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardTrackingServiceTest {
    @Mock UserDailyMetricRepository dailyMetricRepository;
    @Mock UserTopicMasteryRepository topicMasteryRepository;
    @Mock UserRecentActivityRepository recentActivityRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock UserRepository userRepository;
    @InjectMocks DashboardTrackingService trackingService;

    @Test
    void logStudyTimeAccumulatesAndAwardsStreakWhenDailyTargetIsReached() {
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 10, 31);
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .currentStreak(4)
                .longestStreak(4)
                .dailyTargetMinutes(5)
                .lastActiveDate(today.minusDays(1))
                .build();

        when(dailyMetricRepository.findTodayStudyTimeSeconds(userId))
                .thenReturn(Optional.of(240), Optional.of(300));
        when(dailyMetricRepository.getCurrentDatabaseDate()).thenReturn(today);
        when(userProfileRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(profile));

        trackingService.logStudyTime(userId, 120);
        assertThat(profile.getCurrentStreak()).isEqualTo(4);

        trackingService.logStudyTime(userId, 120);

        verify(dailyMetricRepository, org.mockito.Mockito.times(2)).addStudyTime(userId, 120);
        assertThat(profile.getCurrentStreak()).isEqualTo(5);
        assertThat(profile.getLongestStreak()).isEqualTo(5);
        assertThat(profile.getLastActiveDate()).isEqualTo(today);
    }

    @Test
    void logStudyTimeDoesNotAwardStreakBelowDailyTarget() {
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 10, 31);
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .currentStreak(2)
                .longestStreak(3)
                .dailyTargetMinutes(10)
                .lastActiveDate(today.minusDays(1))
                .build();
        when(dailyMetricRepository.findTodayStudyTimeSeconds(userId)).thenReturn(Optional.of(599));
        when(dailyMetricRepository.getCurrentDatabaseDate()).thenReturn(today);
        when(userProfileRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(profile));

        trackingService.logStudyTime(userId, 60);

        assertThat(profile.getCurrentStreak()).isEqualTo(2);
        assertThat(profile.getLastActiveDate()).isEqualTo(today.minusDays(1));
    }

    @Test
    void logStudyTimeDoesNotAwardStreakTwiceOnSameDay() {
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 10, 31);
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .currentStreak(5)
                .longestStreak(5)
                .dailyTargetMinutes(1)
                .lastActiveDate(today)
                .build();
        when(dailyMetricRepository.findTodayStudyTimeSeconds(userId)).thenReturn(Optional.of(600));
        when(dailyMetricRepository.getCurrentDatabaseDate()).thenReturn(today);
        when(userProfileRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(profile));

        trackingService.logStudyTime(userId, 60);

        assertThat(profile.getCurrentStreak()).isEqualTo(5);
        assertThat(profile.getLastActiveDate()).isEqualTo(today);
    }

    @Test
    void logExerciseSolvedCapsTopicMasteryAtOneHundredPercent() {
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        User userReference = User.builder().id(userId).build();
        UserTopicMastery mastery = new UserTopicMastery();
        mastery.setId(new UserTopicMasteryId(userId, SqlTopicCode.JOINS));
        mastery.setMasteryPercentage(new BigDecimal("98.50"));
        mastery.setExercisesSolvedInTopic(19);

        when(userRepository.getReferenceById(userId)).thenReturn(userReference);
        when(topicMasteryRepository.findByIdForUpdate(mastery.getId())).thenReturn(Optional.of(mastery));

        trackingService.logExerciseSolved(userId, SqlTopicCode.JOINS, exerciseId, "Join practice", 90);

        ArgumentCaptor<UserTopicMastery> masteryCaptor = ArgumentCaptor.forClass(UserTopicMastery.class);
        verify(topicMasteryRepository).save(masteryCaptor.capture());
        assertThat(masteryCaptor.getValue().getMasteryPercentage()).isEqualByComparingTo("100.00");
        assertThat(masteryCaptor.getValue().getExercisesSolvedInTopic()).isEqualTo(20);
        verify(dailyMetricRepository).incrementExercisesSolved(userId, 90);
        verify(recentActivityRepository).save(org.mockito.ArgumentMatchers.argThat(activity ->
                activity.getReferenceId().equals(exerciseId)));
    }
}
