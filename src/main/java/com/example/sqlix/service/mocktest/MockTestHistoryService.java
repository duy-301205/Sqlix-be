package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.HistoryFilterRequest;
import com.example.sqlix.dto.response.MockTestHistoryItemDto;
import com.example.sqlix.dto.response.MockTestHistorySummaryDto;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.entity.UserMockTestSummary;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.repository.UserMockTestSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MockTestHistoryService {
    private final TestSessionRepository sessionRepository;
    private final UserMockTestSummaryRepository summaryRepository;

    public MockTestHistorySummaryDto getUserHistorySummary(UUID userId) {
        return summaryRepository.findById(userId).map(this::toSummary)
                .orElseGet(() -> new MockTestHistorySummaryDto(0, 0d, 0, 0d));
    }

    public Page<MockTestHistoryItemDto> getUserHistoryList(UUID userId, HistoryFilterRequest request) {
        HistoryFilterRequest filter = request == null ? new HistoryFilterRequest() : request;
        int page = Math.max(0, filter.getPage());
        int size = Math.max(1, Math.min(filter.getSize(), 100));
        String sortField = "total_score".equalsIgnoreCase(filter.getSortBy()) ? "totalScore" : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        String keyword = filter.getKeyword() == null || filter.getKeyword().isBlank() ? null : filter.getKeyword().trim();
        Boolean passed = null;
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            String status = filter.getStatus().trim().toUpperCase(Locale.ROOT);
            if ("PASSED".equals(status) || "PASS".equals(status)) passed = true;
            else if ("FAILED".equals(status) || "NOT_PASSED".equals(status)) passed = false;
            else if (!"ALL".equals(status) && !"COMPLETED".equals(status) && !"TIMEOUT".equals(status))
                throw new IllegalArgumentException("Unsupported history status: " + filter.getStatus());
        }
        return sessionRepository.findHistory(userId, keyword, passed, filter.getDifficulty(), pageable).map(this::toItem);
    }

    private MockTestHistorySummaryDto toSummary(UserMockTestSummary row) {
        return new MockTestHistorySummaryDto(n(row.getTotalTestsTaken()), d(row.getAverageScore()),
                row.getHighestScore() == null ? 0 : row.getHighestScore(), d(row.getPassRatePercentage()));
    }
    private MockTestHistoryItemDto toItem(TestSession s) {
        return new MockTestHistoryItemDto(s.getId(), s.getTest().getId(), s.getTest().getTitle(), s.getTest().getDifficulty(),
                s.getEndTime(), n(s.getDurationSeconds()), n(s.getCorrectCount()), n(s.getTotalQuestions()),
                n(s.getTotalScore()), Boolean.TRUE.equals(s.getIsPassed()), s.getStatus());
    }
    private long n(Long value){return value == null ? 0 : value;}
    private int n(Integer value){return value == null ? 0 : value;}
    private double d(Double value){return value == null ? 0 : value;}
}
