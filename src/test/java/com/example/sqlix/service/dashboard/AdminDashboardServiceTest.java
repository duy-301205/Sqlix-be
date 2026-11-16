package com.example.sqlix.service.dashboard;

import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.repository.UserExerciseProgressRepository;
import com.example.sqlix.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {
    @Mock UserRepository userRepository;
    @Mock UserExerciseProgressRepository progressRepository;
    @Mock TestSessionRepository sessionRepository;
    @InjectMocks AdminDashboardService service;

    @Test
    void includesRecentFinishedSessionsWithOriginalScoreScale() {
        var user = com.example.sqlix.entity.User.builder().id(java.util.UUID.randomUUID())
                .fullName("Student").email("student@example.com").build();
        var test = new com.example.sqlix.entity.MockTest();
        test.setTitle("SQL exam");
        test.setMaxScore(100);
        var session = new com.example.sqlix.entity.TestSession();
        session.setId(java.util.UUID.randomUUID());
        session.setUser(user); session.setTest(test);
        session.setTotalScore(85); session.setIsPassed(true); session.setDurationSeconds(65);
        session.setEndTime(java.time.Instant.parse("2026-11-16T12:00:00Z"));
        when(sessionRepository.findTop10ByStatusInOrderByEndTimeDescIdDesc(
                java.util.List.of(TestSessionStatus.COMPLETED, TestSessionStatus.TIMEOUT)))
                .thenReturn(java.util.List.of(session));
        var result = service.getSystemStats().recentTestSessions();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().score()).isEqualTo(85);
        assertThat(result.getFirst().maxScore()).isEqualTo(100);
        assertThat(result.getFirst().userName()).isEqualTo("Student");
        assertThat(result.getFirst().isPassed()).isTrue();
        assertThat(result.getFirst().submittedAt()).isEqualTo(session.getEndTime());
    }

    @Test
    void usesAllFinishedAttemptsForAverageAndActualPassedResults() {
        var finished = org.mockito.Mockito.mock(com.example.sqlix.repository.FinishedTestStatsProjection.class);
        when(sessionRepository.getFinishedTestStats()).thenReturn(finished);
        when(finished.getFinishedCount()).thenReturn(3L);
        when(finished.getPassedCount()).thenReturn(1L);
        when(finished.getAverageScore()).thenReturn(66.6666666667);
        var result = service.getSystemStats();
        assertThat(result.totalTestSessions()).isEqualTo(3);
        assertThat(result.averageTestScore()).isEqualTo(66.67);
        assertThat(result.passRatePercent()).isEqualTo(33.33);
    }

    @Test
    void noFinishedAttemptsHaveZeroAverageAndPassRate() {
        var finished = org.mockito.Mockito.mock(com.example.sqlix.repository.FinishedTestStatsProjection.class);
        when(sessionRepository.getFinishedTestStats()).thenReturn(finished);
        when(finished.getFinishedCount()).thenReturn(0L);
        when(finished.getAverageScore()).thenReturn(0.0);
        var result = service.getSystemStats();
        assertThat(result.totalTestSessions()).isZero();
        assertThat(result.averageTestScore()).isZero();
        assertThat(result.passRatePercent()).isZero();
    }

    @Test
    void aggregatesSystemCounters() {
        when(userRepository.count()).thenReturn(120L);
        when(progressRepository.countByIsPassedTrue()).thenReturn(840L);
        when(sessionRepository.count()).thenReturn(53L);
        when(sessionRepository.countByStatus(TestSessionStatus.COMPLETED)).thenReturn(47L);

        var stats = service.getSystemStats();

        assertThat(stats.totalUsers()).isEqualTo(120);
        assertThat(stats.exercisesCompleted()).isEqualTo(840);
        assertThat(stats.totalMockTestSessions()).isEqualTo(53);
        assertThat(stats.completedMockTestSessions()).isEqualTo(47);
    }
}
