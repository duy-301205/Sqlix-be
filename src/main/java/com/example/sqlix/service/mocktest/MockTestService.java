package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.response.MockTestCatalogDto;
import com.example.sqlix.dto.response.MockTestDetailDto;
import com.example.sqlix.dto.response.SessionStartResponse;
import com.example.sqlix.dto.response.TestWorkspaceDto;
import com.example.sqlix.dto.response.WorkspaceQuestionDto;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.MockTestQuestionRepository;
import com.example.sqlix.repository.MockTestRepository;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MockTestService {
    private final MockTestRepository mockTestRepository;
    private final MockTestQuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<MockTestCatalogDto> getCatalog(
            String search,
            DifficultyLevel difficulty,
            int page,
            int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("title").ascending());
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        return mockTestRepository.searchCatalog(normalizedSearch, difficulty, pageable)
                .map(this::toCatalogDto);
    }

    @Transactional(readOnly = true)
    public MockTestDetailDto getDetail(String slug) {
        MockTest test = mockTestRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        return toDetailDto(test);
    }

    @Transactional
    public SessionStartResponse startSession(UUID userId, UUID testId) {
        MockTest test = mockTestRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        TestSession session = new TestSession();
        session.setUser(userRepository.getReferenceById(userId));
        session.setTest(test);
        session.setStatus(TestSessionStatus.IN_PROGRESS);
        session.setStartTime(Instant.now());
        session.setTotalScore(0);
        TestSession saved = sessionRepository.save(session);
        return new SessionStartResponse(
                saved.getId(), saved.getStartTime(), valueOrZero(test.getDurationMinutes()), saved.getStatus());
    }

    @Transactional(readOnly = true)
    public TestWorkspaceDto getWorkspace(UUID userId, UUID sessionId) {
        TestSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_SESSION_NOT_FOUND));
        MockTest test = session.getTest();
        List<WorkspaceQuestionDto> questions = questionRepository
                .findAllByTest_IdOrderBySortOrderAsc(test.getId()).stream()
                .map(this::toWorkspaceQuestion)
                .toList();
        return new TestWorkspaceDto(
                session.getId(), session.getStartTime(), valueOrZero(test.getDurationMinutes()), questions);
    }

    private WorkspaceQuestionDto toWorkspaceQuestion(MockTestQuestion question) {
        return new WorkspaceQuestionDto(
                question.getId(),
                question.getQuestionType(),
                question.getContent(),
                valueOrZero(question.getPoints()),
                valueOrZero(question.getSortOrder()),
                question.getRequirements(),
                question.getExpectedColumns(),
                question.getMcOptions(),
                question.getDatabase() == null ? null : question.getDatabase().getId(),
                question.getDatabase() == null ? null : question.getDatabase().getSchemaData());
    }

    private MockTestCatalogDto toCatalogDto(MockTest test) {
        return new MockTestCatalogDto(
                test.getId(), test.getTitle(), test.getSlug(), test.getDescription(), test.getDifficulty(),
                valueOrZero(test.getDurationMinutes()), valueOrZero(test.getTotalQuestions()),
                test.getCoverImageUrl(), valueOrZero(test.getTotalParticipants()));
    }

    private MockTestDetailDto toDetailDto(MockTest test) {
        return new MockTestDetailDto(
                test.getId(), test.getTitle(), test.getSlug(), test.getDescription(), test.getDifficulty(),
                valueOrZero(test.getDurationMinutes()), valueOrZero(test.getTotalQuestions()),
                test.getCoverImageUrl(), valueOrZero(test.getTotalParticipants()));
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
