package com.example.sqlix.service.dashboard;

import com.example.sqlix.dto.response.AdminDashboardStatsDto;
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
        return new AdminDashboardStatsDto(
                userRepository.count(),
                exerciseProgressRepository.countByIsPassedTrue(),
                testSessionRepository.count(),
                testSessionRepository.countByStatus(TestSessionStatus.COMPLETED));
    }
}
