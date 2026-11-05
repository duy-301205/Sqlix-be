package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.AdminHistoryFilterRequest;
import com.example.sqlix.dto.response.AdminHistoryItemDto;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.TestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMockTestHistoryService {
    private final TestSessionRepository sessionRepository;

    public Page<AdminHistoryItemDto> getAllHistories(AdminHistoryFilterRequest request) {
        AdminHistoryFilterRequest filter = request == null ? new AdminHistoryFilterRequest() : request;
        int page = Math.max(filter.getPage(), 0), size = Math.max(1, Math.min(filter.getSize(), 100));
        String property = switch (filter.getSortBy() == null ? "" : filter.getSortBy().toLowerCase()) {
            case "total_score", "score" -> "totalScore";
            case "submitted_at", "end_time" -> "endTime";
            case "username" -> "user.username";
            case "test_title" -> "test.title";
            default -> "createdAt";
        };
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        return sessionRepository.findAdminHistory(filter.getUserId(), filter.getTestId(), filter.getFromDate(),
                filter.getToDate(), filter.getMinScore(), filter.getMaxScore(), pageable).map(this::toDto);
    }

    @Transactional
    public void adminDeleteSession(UUID sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_SESSION_NOT_FOUND));
        sessionRepository.delete(session);
    }

    private AdminHistoryItemDto toDto(TestSession s) {
        return new AdminHistoryItemDto(s.getId(), s.getUser().getId(), s.getUser().getUsername(), s.getUser().getFullName(),
                s.getTest().getTitle(), s.getTotalScore() == null ? 0 : s.getTotalScore(), s.getEndTime(),
                s.getDurationSeconds() == null ? 0 : s.getDurationSeconds(), s.getStatus(), Boolean.TRUE.equals(s.getIsPassed()));
    }
}
