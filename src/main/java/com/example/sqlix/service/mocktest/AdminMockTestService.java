package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.MockTestQuestionRequest;
import com.example.sqlix.dto.request.MockTestRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMockTestService {
    private final MockTestRepository mockTestRepository;
    private final MockTestQuestionRepository questionRepository;
    private final PracticeDatabaseRepository practiceDatabaseRepository;

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
        MockTestQuestion question = questionRepository.findForUpdateById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.MOCK_TEST_QUESTION_NOT_FOUND));
        applyQuestionRequest(question, request);
        return questionRepository.save(question);
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
