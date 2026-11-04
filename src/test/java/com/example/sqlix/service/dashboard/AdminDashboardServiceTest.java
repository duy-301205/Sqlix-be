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
