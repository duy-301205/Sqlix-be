package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.HistoryFilterRequest;
import com.example.sqlix.dto.response.MockTestHistoryItemDto;
import com.example.sqlix.dto.response.MockTestHistorySummaryDto;
import com.example.sqlix.dto.response.MockTestHistoryDetailDto;
import com.example.sqlix.dto.response.ProgressTrendDto;
import com.example.sqlix.entity.TestSessionAnswer;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.repository.TestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MockTestHistoryService {
    private final TestSessionRepository sessionRepository;

    public MockTestHistorySummaryDto getUserHistorySummary(UUID userId) {
        return sessionRepository.findHistorySummaryByUserId(userId).map(this::toSummary)
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

    public MockTestHistoryDetailDto getHistoryDetail(UUID userId, UUID sessionId) {
        TestSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_SESSION_NOT_FOUND));
        if (session.getStatus() != TestSessionStatus.COMPLETED && session.getStatus() != TestSessionStatus.TIMEOUT) {
            throw new AppException(ErrorCode.EXAM_NOT_IN_PROGRESS);
        }
        List<MockTestHistoryDetailDto.QuestionDetail> questions = session.getAnswers().stream()
                .sorted(Comparator.comparingInt(answer -> answer.getQuestion().getSortOrder()))
                .map(this::toQuestionDetail).toList();
        return new MockTestHistoryDetailDto(session.getId(), session.getTest().getId(), session.getTest().getTitle(),
                session.getTest().getDifficulty(), session.getEndTime(), n(session.getDurationSeconds()),
                n(session.getTotalScore()), Boolean.TRUE.equals(session.getIsPassed()), session.getStatus(), questions);
    }

    private MockTestHistoryDetailDto.QuestionDetail toQuestionDetail(TestSessionAnswer answer) {
        var question = answer.getQuestion();
        String expected = question.getQuestionType() == com.example.sqlix.enums.QuestionType.MULTIPLE_CHOICE
                ? question.getMcCorrectOption() : question.getSolutionQuery();
        return new MockTestHistoryDetailDto.QuestionDetail(question.getId(), question.getQuestionType().name(),
                question.getContent(), answer.getSubmittedAnswer(), expected, question.getExplanation(),
                n(answer.getScore()), n(question.getPoints()), Boolean.TRUE.equals(answer.getIsCorrect()));
    }

    public List<ProgressTrendDto> getScoreTrends(UUID userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        var statuses = List.of(TestSessionStatus.COMPLETED, TestSessionStatus.TIMEOUT);
        return sessionRepository.findByUser_IdAndStatusInOrderByCreatedAtDesc(userId, statuses,
                        PageRequest.of(0, safeLimit))
                .stream().sorted(Comparator.comparing(TestSession::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(session -> new ProgressTrendDto(session.getEndTime(), n(session.getTotalScore()),
                        session.getTest().getTitle(), Boolean.TRUE.equals(session.getIsPassed())))
                .toList();
    }

    @Transactional
    public void deleteUserSession(UUID userId, UUID sessionId) {
        TestSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_SESSION_NOT_FOUND));
        sessionRepository.delete(session);
    }

    private MockTestHistorySummaryDto toSummary(com.example.sqlix.repository.UserMockTestSummaryProjection row) {
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
