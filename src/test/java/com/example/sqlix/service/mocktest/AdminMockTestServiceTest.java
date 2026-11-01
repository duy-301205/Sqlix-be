package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.MockTestQuestionRequest;
import com.example.sqlix.dto.request.MockTestRequest;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.QuestionType;
import com.example.sqlix.repository.MockTestQuestionRepository;
import com.example.sqlix.repository.MockTestRepository;
import com.example.sqlix.repository.PracticeDatabaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMockTestServiceTest {
    @Mock MockTestRepository mockTestRepository;
    @Mock MockTestQuestionRepository questionRepository;
    @Mock PracticeDatabaseRepository practiceDatabaseRepository;
    @InjectMocks AdminMockTestService service;

    @Test
    void createMockTestStartsWithZeroQuestions() {
        when(mockTestRepository.existsBySlug("sql-basics")).thenReturn(false);
        when(mockTestRepository.save(any(MockTest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockTest result = service.createMockTest(testRequest());

        assertThat(result.getTotalQuestions()).isZero();
        assertThat(result.getSlug()).isEqualTo("sql-basics");
    }

    @Test
    void createQuestionIncrementsParentQuestionCount() {
        UUID testId = UUID.randomUUID();
        MockTest test = mockTest(testId, 4);
        when(mockTestRepository.findByIdForUpdate(testId)).thenReturn(Optional.of(test));
        when(questionRepository.save(any(MockTestQuestion.class))).thenAnswer(invocation -> {
            MockTestQuestion question = invocation.getArgument(0);
            question.setId(UUID.randomUUID());
            return question;
        });

        MockTestQuestion created = service.createQuestion(testId, multipleChoiceRequest());

        assertThat(created.getQuestionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(test.getTotalQuestions()).isEqualTo(5);
        verify(mockTestRepository).save(test);
    }

    @Test
    void createSqlQuestionLinksPracticeDatabase() {
        UUID testId = UUID.randomUUID();
        UUID databaseId = UUID.randomUUID();
        MockTest test = mockTest(testId, 0);
        PracticeDatabase database = new PracticeDatabase();
        database.setId(databaseId);
        when(mockTestRepository.findByIdForUpdate(testId)).thenReturn(Optional.of(test));
        when(practiceDatabaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(questionRepository.save(any(MockTestQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockTestQuestion created = service.createQuestion(testId, sqlRequest(databaseId));

        assertThat(created.getDatabase()).isSameAs(database);
        assertThat(created.getSolutionQuery()).isEqualTo("SELECT id FROM users");
        assertThat(test.getTotalQuestions()).isEqualTo(1);
    }

    @Test
    void deleteQuestionDecrementsParentQuestionCountWithoutGoingBelowZero() {
        UUID testId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        MockTest test = mockTest(testId, 1);
        MockTestQuestion question = new MockTestQuestion();
        question.setId(questionId);
        question.setTest(test);
        when(questionRepository.findForUpdateById(questionId)).thenReturn(Optional.of(question));
        when(mockTestRepository.findByIdForUpdate(testId)).thenReturn(Optional.of(test));

        service.deleteQuestion(questionId);

        assertThat(test.getTotalQuestions()).isZero();
        verify(questionRepository).delete(question);
        verify(mockTestRepository).save(test);
    }

    @Test
    void updatesQuestionFields() {
        UUID testId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        MockTestQuestion question = new MockTestQuestion();
        question.setId(questionId);
        question.setTest(mockTest(testId, 2));
        when(questionRepository.findForUpdateById(questionId)).thenReturn(Optional.of(question));
        when(questionRepository.save(question)).thenReturn(question);

        MockTestQuestion updated = service.updateQuestion(questionId, multipleChoiceRequest());

        assertThat(updated.getMcCorrectOption()).isEqualTo("A");
        assertThat(updated.getMcOptions()).containsExactly(Map.of("id", "A", "text", "SELECT 1"));
    }

    private MockTestRequest testRequest() {
        return new MockTestRequest("SQL Basics", "sql-basics", "Intro test", DifficultyLevel.EASY,
                30, "cover.png");
    }

    private MockTestQuestionRequest multipleChoiceRequest() {
        return new MockTestQuestionRequest(QuestionType.MULTIPLE_CHOICE, null, "Choose a query", 10, 1,
                null, null, null, null,
                List.of(Map.of("id", "A", "text", "SELECT 1")), "A", "A returns one row");
    }

    private MockTestQuestionRequest sqlRequest(UUID databaseId) {
        return new MockTestQuestionRequest(QuestionType.SQL_QUERY, databaseId, "Return user ids", 10, 1,
                List.of("Use users table"), List.of("id"), "SELECT id FROM users", 2,
                null, null, null);
    }

    private MockTest mockTest(UUID id, int count) {
        MockTest test = new MockTest();
        test.setId(id);
        test.setTotalQuestions(count);
        return test;
    }
}
