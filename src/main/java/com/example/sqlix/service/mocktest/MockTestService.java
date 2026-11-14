package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.response.MockTestCatalogDto;
import com.example.sqlix.dto.response.MockTestDetailDto;
import com.example.sqlix.dto.request.AnswerRequestDto;
import com.example.sqlix.dto.response.AnswerResponseDto;
import com.example.sqlix.dto.response.QueryResultDto;
import com.example.sqlix.dto.response.QuestionResultDto;
import com.example.sqlix.dto.response.SessionStartResponse;
import com.example.sqlix.dto.response.TestResultDto;
import com.example.sqlix.dto.response.TestWorkspaceDto;
import com.example.sqlix.dto.response.WorkspaceQuestionDto;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.entity.TestSessionAnswer;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.QuestionType;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.MockTestQuestionRepository;
import com.example.sqlix.repository.MockTestRepository;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.repository.TestSessionAnswerRepository;
import com.example.sqlix.service.practice.SqlExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MockTestService {
    private final MockTestRepository mockTestRepository;
    private final MockTestQuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TestSessionAnswerRepository answerRepository;
    private final SqlExecutionService sqlExecutionService;

    private static final int MAX_RESULT_ROWS = 10_001;

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
        
        Specification<MockTest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isPublished")));
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)
                ));
            }
            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return mockTestRepository.findAll(spec, pageable)
                .map(this::toCatalogDto);
    }

    @Transactional(readOnly = true)
    public MockTestDetailDto getDetail(String slug) {
        MockTest test = null;
        try {
            UUID id = UUID.fromString(slug);
            test = mockTestRepository.findById(id).orElse(null);
        } catch (IllegalArgumentException ignored) {}

        if (test == null) {
            test = mockTestRepository.findBySlug(slug)
                    .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        }
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

    @Transactional(noRollbackFor = AppException.class)
    public AnswerResponseDto submitAnswer(UUID userId, UUID sessionId, UUID questionId, AnswerRequestDto request) {
        TestSession session = getOwnedSession(userId, sessionId);
        ensureInProgress(session);
        if (isExpired(session, Instant.now())) {
            closeAsTimeout(session);
            throw new AppException(ErrorCode.EXAM_SESSION_EXPIRED);
        }

        MockTestQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        if (!question.getTest().getId().equals(session.getTest().getId())) {
            throw new AppException(ErrorCode.MOCK_TEST_NOT_FOUND);
        }

        TestSessionAnswer answer = answerRepository.findBySession_IdAndQuestion_Id(sessionId, questionId)
                .orElseGet(() -> {
                    TestSessionAnswer created = new TestSessionAnswer();
                    created.setSession(session);
                    created.setQuestion(question);
                    created.setRetryCount(0);
                    created.setScore(0);
                    created.setIsCorrect(false);
                    return created;
                });
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            answer.setSubmittedAnswer(request == null ? null : request.submittedAnswer());
            answerRepository.save(answer);
            return new AnswerResponseDto(false, 0, valueOrZero(answer.getRetryCount()), "Đáp án đã được lưu.");
        }

        int retries = valueOrZero(answer.getRetryCount());
        int maxRetries = valueOrZero(question.getMaxRetries());
        if (retries >= maxRetries) {
            throw new AppException(ErrorCode.EXAM_RETRY_LIMIT_REACHED);
        }
        answer.setSubmittedAnswer(request == null ? null : request.submittedAnswer());
        answer.setRetryCount(retries + 1);
        QueryResultDto actual = sqlExecutionService.executeSelect(
                question.getDatabase() == null ? null : question.getDatabase().getInitSqlScript(),
                answer.getSubmittedAnswer(), MAX_RESULT_ROWS);
        String feedback = actual.errorMessage();
        boolean correct = false;
        int score = 0;
        if (feedback == null) {
            QueryResultDto expected = sqlExecutionService.executeSelect(
                    question.getDatabase() == null ? null : question.getDatabase().getInitSqlScript(),
                    question.getSolutionQuery(), MAX_RESULT_ROWS);
            if (expected.errorMessage() != null) {
                feedback = "Không thể kiểm tra đáp án chuẩn.";
            } else {
                feedback = compareResults(actual, expected);
                correct = feedback == null;
                if (correct) score = valueOrZero(question.getPoints());
            }
        }
        answer.setIsCorrect(correct);
        answer.setScore(score);
        answerRepository.save(answer);
        return new AnswerResponseDto(correct, score, answer.getRetryCount(), feedback);
    }

    @Transactional(noRollbackFor = AppException.class)
    public TestResultDto submitTest(UUID userId, UUID sessionId) {
        TestSession session = getOwnedSession(userId, sessionId);
        ensureInProgress(session);
        boolean timeout = isExpired(session, Instant.now());
        List<MockTestQuestion> questions = questionRepository
                .findAllByTest_IdOrderBySortOrderAsc(session.getTest().getId());
        List<TestSessionAnswer> answers = answerRepository.findAllBySession_Id(sessionId);
        var answersByQuestion = new java.util.HashMap<UUID, TestSessionAnswer>();
        answers.forEach(answer -> answersByQuestion.put(answer.getQuestion().getId(), answer));

        int totalScore = 0;
        for (MockTestQuestion question : questions) {
            TestSessionAnswer answer = answersByQuestion.get(question.getId());
            if (answer == null) {
                answer = new TestSessionAnswer();
                answer.setSession(session);
                answer.setQuestion(question);
                answer.setRetryCount(0);
                answer.setSubmittedAnswer(null);
            }
            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                boolean correct = answer.getSubmittedAnswer() != null
                        && answer.getSubmittedAnswer().equals(question.getMcCorrectOption());
                answer.setIsCorrect(correct);
                answer.setScore(correct ? valueOrZero(question.getPoints()) : 0);
            }
            totalScore += valueOrZero(answer.getScore());
            if (answer.getId() == null || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                answerRepository.save(answer);
            }
        }
        session.setTotalScore(totalScore);
        session.setEndTime(Instant.now());
        session.setStatus(timeout ? TestSessionStatus.TIMEOUT : TestSessionStatus.COMPLETED);
        sessionRepository.save(session);
        return buildTestResult(session, questions, answerRepository.findAllBySession_Id(sessionId));
    }

    @Transactional(readOnly = true)
    public TestResultDto getTestResult(UUID userId, UUID sessionId) {
        TestSession session = getOwnedSession(userId, sessionId);
        if (session.getStatus() != TestSessionStatus.COMPLETED
                && session.getStatus() != TestSessionStatus.TIMEOUT) {
            throw new AppException(ErrorCode.EXAM_NOT_IN_PROGRESS);
        }
        List<MockTestQuestion> questions = questionRepository
                .findAllByTest_IdOrderBySortOrderAsc(session.getTest().getId());
        return buildTestResult(session, questions, answerRepository.findAllBySession_Id(sessionId));
    }

    private TestResultDto buildTestResult(
            TestSession session, List<MockTestQuestion> questions, List<TestSessionAnswer> answers) {
        var answerByQuestion = new java.util.HashMap<UUID, TestSessionAnswer>();
        answers.forEach(answer -> answerByQuestion.put(answer.getQuestion().getId(), answer));
        List<QuestionResultDto> results = questions.stream().map(question -> {
            TestSessionAnswer answer = answerByQuestion.get(question.getId());
            return new QuestionResultDto(
                    question.getId(), question.getQuestionType(), question.getContent(),
                    valueOrZero(question.getPoints()), valueOrZero(question.getSortOrder()),
                    question.getRequirements(), question.getExpectedColumns(), question.getMcOptions(),
                    question.getDatabase() == null ? null : question.getDatabase().getId(),
                    question.getDatabase() == null ? null : question.getDatabase().getSchemaData(),
                    answer == null ? null : answer.getSubmittedAnswer(),
                    answer == null ? 0 : valueOrZero(answer.getScore()),
                    answer != null && Boolean.TRUE.equals(answer.getIsCorrect()),
                    question.getSolutionQuery(), question.getMcCorrectOption(), question.getExplanation());
        }).toList();
        Instant end = session.getEndTime() == null ? Instant.now() : session.getEndTime();
        Duration elapsed = Duration.between(session.getStartTime(), end);
        return new TestResultDto(session.getId(), valueOrZero(session.getTotalScore()), session.getStatus(),
                elapsed.isNegative() ? 0 : elapsed.toSeconds(), results);
    }

    private TestSession getOwnedSession(UUID userId, UUID sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_SESSION_NOT_FOUND));
    }

    private void ensureInProgress(TestSession session) {
        if (session.getStatus() != TestSessionStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.EXAM_NOT_IN_PROGRESS);
        }
    }

    private boolean isExpired(TestSession session, Instant now) {
        int duration = valueOrZero(session.getTest().getDurationMinutes());
        return session.getStartTime().plusSeconds(duration * 60L).isBefore(now);
    }

    private void closeAsTimeout(TestSession session) {
        session.setStatus(TestSessionStatus.TIMEOUT);
        session.setEndTime(Instant.now());
        sessionRepository.saveAndFlush(session);
    }

    private String compareResults(QueryResultDto actual, QueryResultDto expected) {
        if (!Objects.equals(actual.columns(), expected.columns())) return "Tên hoặc số lượng cột không khớp.";
        if (!Objects.equals(actual.rows(), expected.rows())) return "Dữ liệu hoặc số lượng dòng không khớp.";
        return null;
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
                test.getCoverImageUrl(), valueOrZero(test.getTotalParticipants()),
                test.getPassingScore() == null ? 7.0 : test.getPassingScore(),
                test.getMaxScore() == null ? 10 : test.getMaxScore(),
                test.getIsPublished() == null ? true : test.getIsPublished());
    }

    private MockTestDetailDto toDetailDto(MockTest test) {
        return new MockTestDetailDto(
                test.getId(), test.getTitle(), test.getSlug(), test.getDescription(), test.getDifficulty(),
                valueOrZero(test.getDurationMinutes()), valueOrZero(test.getTotalQuestions()),
                test.getCoverImageUrl(), valueOrZero(test.getTotalParticipants()),
                test.getPassingScore() == null ? 7.0 : test.getPassingScore(),
                test.getMaxScore() == null ? 10 : test.getMaxScore(),
                test.getIsPublished() == null ? true : test.getIsPublished());
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
