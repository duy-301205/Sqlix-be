package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.MockTestQuestionRequest;
import com.example.sqlix.dto.request.MockTestRequest;
import com.example.sqlix.dto.response.AdminMockTestQuestionDto;
import com.example.sqlix.dto.response.MockTestCatalogDto;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.enums.QuestionType;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.MockTestQuestionRepository;
import com.example.sqlix.repository.MockTestRepository;
import com.example.sqlix.repository.PracticeDatabaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMockTestService {
    private final MockTestRepository mockTestRepository;
    private final MockTestQuestionRepository questionRepository;
    private final PracticeDatabaseRepository practiceDatabaseRepository;

    @Transactional(readOnly = true)
    public List<MockTestCatalogDto> getAllMockTests() {
        return mockTestRepository.findAll(Sort.by("title").ascending())
                .stream()
                .map(test -> new MockTestCatalogDto(
                        test.getId(),
                        test.getTitle(),
                        test.getSlug(),
                        test.getDescription(),
                        test.getDifficulty(),
                        valueOrZero(test.getDurationMinutes()),
                        valueOrZero(test.getTotalQuestions()),
                        test.getCoverImageUrl(),
                        valueOrZero(test.getTotalParticipants()),
                        test.getPassingScore() == null ? 7.0 : test.getPassingScore(),
                        test.getMaxScore() == null ? 10 : test.getMaxScore(),
                        test.getIsPublished() == null ? true : test.getIsPublished()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminMockTestQuestionDto> getQuestionsByTestId(UUID testId) {
        return questionRepository.findAllByTest_IdOrderBySortOrderAsc(testId)
                .stream()
                .map(q -> new AdminMockTestQuestionDto(
                        q.getId(),
                        q.getTest().getId(),
                        q.getQuestionType(),
                        q.getDatabase() != null ? q.getDatabase().getId() : null,
                        q.getContent(),
                        q.getPoints(),
                        q.getSortOrder(),
                        q.getRequirements(),
                        q.getExpectedColumns(),
                        q.getSolutionQuery(),
                        q.getMaxRetries(),
                        q.getMcOptions(),
                        q.getMcCorrectOption(),
                        q.getExplanation()
                ))
                .toList();
    }

    @Transactional
    public MockTest createMockTest(MockTestRequest request) {
        if (mockTestRepository.existsBySlug(request.slug())) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
        MockTest test = new MockTest();
        applyMockTestRequest(test, request);
        test.setTotalQuestions(0);
        return mockTestRepository.save(test);
    }

    @Transactional
    public MockTest updateMockTest(UUID id, MockTestRequest request) {
        MockTest test = mockTestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        if (mockTestRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
        applyMockTestRequest(test, request);
        return mockTestRepository.save(test);
    }

    @Transactional
    public void deleteMockTest(UUID id) {
        MockTest test = mockTestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        mockTestRepository.delete(test);
    }

    @Transactional
    public MockTestQuestion createQuestion(UUID testId, MockTestQuestionRequest request) {
        MockTest test = mockTestRepository.findByIdForUpdate(testId)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        MockTestQuestion question = new MockTestQuestion();
        question.setTest(test);
        applyQuestionRequest(question, request);
        MockTestQuestion saved = questionRepository.save(question);
        test.setTotalQuestions(valueOrZero(test.getTotalQuestions()) + 1);
        mockTestRepository.save(test);
        return saved;
    }

    @Transactional
    public MockTestQuestion updateQuestion(UUID questionId, MockTestQuestionRequest request) {
        MockTest question = null;
        MockTestQuestion q = questionRepository.findForUpdateById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_QUESTION_NOT_FOUND));
        applyQuestionRequest(q, request);
        return questionRepository.save(q);
    }

    @Transactional
    public void deleteQuestion(UUID questionId) {
        MockTestQuestion question = questionRepository.findForUpdateById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_QUESTION_NOT_FOUND));
        UUID testId = question.getTest().getId();
        MockTest test = mockTestRepository.findByIdForUpdate(testId)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_NOT_FOUND));
        questionRepository.delete(question);
        test.setTotalQuestions(Math.max(0, valueOrZero(test.getTotalQuestions()) - 1));
        mockTestRepository.save(test);
    }

    private void applyMockTestRequest(MockTest test, MockTestRequest request) {
        test.setTitle(request.title());
        test.setSlug(request.slug());
        test.setDescription(request.description());
        test.setDifficulty(request.difficulty());
        test.setDurationMinutes(request.durationMinutes());
        test.setCoverImageUrl(request.coverImageUrl());
        test.setPassingScore(request.passingScore() == null ? 7.0 : request.passingScore());
        test.setMaxScore(request.maxScore() == null ? 10 : request.maxScore());
        test.setIsPublished(request.isPublished() == null ? true : request.isPublished());
    }

    private void applyQuestionRequest(MockTestQuestion question, MockTestQuestionRequest request) {
        question.setQuestionType(request.questionType());
        question.setContent(request.content());
        question.setPoints(request.points());
        question.setSortOrder(request.sortOrder());
        question.setMaxRetries(request.maxRetries() == null ? 3 : request.maxRetries());

        if (request.questionType() == QuestionType.SQL_QUERY) {
            if (request.databaseId() == null || request.solutionQuery() == null || request.solutionQuery().isBlank()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
            PracticeDatabase database = practiceDatabaseRepository.findById(request.databaseId())
                    .orElseThrow(() -> new AppException(ErrorCode.DATABASE_CATALOG_NOT_FOUND));
            question.setDatabase(database);
            question.setRequirements(request.requirements());
            question.setExpectedColumns(request.expectedColumns());
            question.setSolutionQuery(request.solutionQuery());
            question.setMcOptions(null);
            question.setMcCorrectOption(null);
            question.setExplanation(null);
        } else {
            if (request.mcOptions() == null || request.mcOptions().isEmpty()
                    || request.mcCorrectOption() == null || request.mcCorrectOption().isBlank()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
            question.setDatabase(null);
            question.setRequirements(null);
            question.setExpectedColumns(null);
            question.setSolutionQuery(null);
            question.setMcOptions(request.mcOptions());
            question.setMcCorrectOption(request.mcCorrectOption());
            question.setExplanation(request.explanation());
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
