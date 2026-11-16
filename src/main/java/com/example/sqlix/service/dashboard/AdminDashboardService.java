package com.example.sqlix.service.dashboard;

import com.example.sqlix.dto.response.AdminDashboardStatsDto;
import com.example.sqlix.dto.response.RecentAdminTestSessionDto;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.repository.UserExerciseProgressRepository;
import com.example.sqlix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final UserRepository userRepository;
    private final UserExerciseProgressRepository exerciseProgressRepository;
    private final TestSessionRepository testSessionRepository;

    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getSystemStats() {
        var finished = testSessionRepository.getFinishedTestStats();
        long submittedCount = finished == null ? 0 : finished.getFinishedCount();
        double averageScore = finished == null ? 0 : Math.round(finished.getAverageScore() * 100.0) / 100.0;
        double passRate = submittedCount == 0 ? 0
                : Math.round(finished.getPassedCount() * 10000.0 / submittedCount) / 100.0;
        return new AdminDashboardStatsDto(
                userRepository.count(),
                exerciseProgressRepository.countByIsPassedTrue(),
                testSessionRepository.count(),
                testSessionRepository.countByStatus(TestSessionStatus.COMPLETED),
                testSessionRepository.findTop10ByStatusInOrderByEndTimeDescIdDesc(
                        java.util.List.of(TestSessionStatus.COMPLETED, TestSessionStatus.TIMEOUT)).stream()
                        .map(session -> new RecentAdminTestSessionDto(
                                session.getId(), session.getUser().getFullName(), session.getUser().getEmail(),
                                session.getTest().getTitle(), session.getTotalScore(), session.getTest().getMaxScore(),
                                Boolean.TRUE.equals(session.getIsPassed()),
                                session.getDurationSeconds() == null ? 0 : session.getDurationSeconds(), session.getEndTime()))
                        .toList(), submittedCount, averageScore, passRate);
    }
}
