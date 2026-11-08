package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.AdminHistoryFilterRequest;
import com.example.sqlix.dto.response.AdminHistoryItemDto;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.TestSessionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

        Specification<TestSession> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(TestSessionStatus.COMPLETED, TestSessionStatus.TIMEOUT));
            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
            }
            if (filter.getTestId() != null) {
                predicates.add(cb.equal(root.get("test").get("id"), filter.getTestId()));
            }
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endTime"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), filter.getToDate()));
            }
            if (filter.getMinScore() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalScore"), filter.getMinScore()));
            }
            if (filter.getMaxScore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalScore"), filter.getMaxScore()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return sessionRepository.findAll(spec, pageable).map(this::toDto);
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
