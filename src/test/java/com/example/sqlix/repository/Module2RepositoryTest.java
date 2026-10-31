package com.example.sqlix.repository;

import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserDailyMetric;
import com.example.sqlix.entity.UserDailyMetricId;
import com.example.sqlix.entity.UserRecentActivity;
import com.example.sqlix.entity.UserTopicMastery;
import com.example.sqlix.entity.UserTopicMasteryId;
import com.example.sqlix.enums.ActivityType;
import com.example.sqlix.enums.SqlTopicCode;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class Module2RepositoryTest {
    @Autowired private UserRepository userRepository;
    @Autowired private UserDailyMetricRepository dailyMetricRepository;
    @Autowired private UserTopicMasteryRepository topicMasteryRepository;
    @Autowired private UserRecentActivityRepository recentActivityRepository;

    private UUID userId;
    private User user;

    @BeforeEach
    void createUserForForeignKeys() {
        user = User.builder()
                .username("analytics_" + UUID.randomUUID().toString().substring(0, 8))
                .email(UUID.randomUUID() + "@example.com")
                .fullName("Analytics Test")
                .occupation(UserOccupation.OTHER)
                .systemRole(UserSystemRole.USER)
                .build();
        userId = userRepository.saveAndFlush(user).getId();
    }

    @Test
    void savesAndFindsDailyMetricByCompositeId() {
        UserDailyMetricId id = new UserDailyMetricId(userId, LocalDate.of(2026, 10, 31));
        UserDailyMetric metric = new UserDailyMetric();
        metric.setId(id);
        metric.setUser(user);
        metric.setStudyTimeSeconds(1800);
        metric.setLessonsCompleted(2);
        metric.setExercisesSolved(5);
        metric.setTotalScore(450);

        dailyMetricRepository.saveAndFlush(metric);
        dailyMetricRepository.flush();

        var reloaded = dailyMetricRepository.findById(id).orElseThrow();
        assertThat(reloaded.getId()).isEqualTo(id);
        assertThat(reloaded.getStudyTimeSeconds()).isEqualTo(1800);
        assertThat(reloaded.getLessonsCompleted()).isEqualTo(2);
        assertThat(reloaded.getExercisesSolved()).isEqualTo(5);
        assertThat(reloaded.getTotalScore()).isEqualTo(450);
    }

    @Test
    void savesAndFindsTopicMasteryByCompositeId() {
        UserTopicMasteryId id = new UserTopicMasteryId(userId, SqlTopicCode.JOINS);
        UserTopicMastery mastery = new UserTopicMastery();
        mastery.setId(id);
        mastery.setUser(user);
        mastery.setMasteryPercentage(new BigDecimal("87.50"));
        mastery.setExercisesSolvedInTopic(14);

        topicMasteryRepository.saveAndFlush(mastery);
        topicMasteryRepository.flush();

        var reloaded = topicMasteryRepository.findById(id).orElseThrow();
        assertThat(reloaded.getId()).isEqualTo(id);
        assertThat(reloaded.getMasteryPercentage()).isEqualByComparingTo("87.50");
        assertThat(reloaded.getExercisesSolvedInTopic()).isEqualTo(14);
    }

    @Test
    void savesAndFindsRecentActivityJsonb() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("score", 90);
        metadata.put("title", "Join 3 tables");
        metadata.put("tags", java.util.List.of("joins", "practice"));

        UserRecentActivity activity = new UserRecentActivity();
        activity.setUser(user);
        activity.setType(ActivityType.EXERCISE_SOLVED);
        activity.setReferenceId(UUID.randomUUID());
        activity.setMetaData(metadata);

        UserRecentActivity saved = recentActivityRepository.saveAndFlush(activity);
        recentActivityRepository.flush();

        var reloaded = recentActivityRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getUser().getId()).isEqualTo(userId);
        assertThat(reloaded.getType()).isEqualTo(ActivityType.EXERCISE_SOLVED);
        assertThat(reloaded.getReferenceId()).isEqualTo(activity.getReferenceId());
        assertThat(reloaded.getMetaData()).isEqualTo(metadata);
    }
}
