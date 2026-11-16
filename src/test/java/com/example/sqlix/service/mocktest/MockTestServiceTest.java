package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.response.WorkspaceQuestionDto;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.QuestionType;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.repository.MockTestQuestionRepository;
import com.example.sqlix.repository.MockTestRepository;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockTestServiceTest {
    @Mock MockTestRepository mockTestRepository;
    @Mock MockTestQuestionRepository questionRepository;
    @Mock TestSessionRepository sessionRepository;
    @Mock UserRepository userRepository;
    @Mock com.example.sqlix.repository.TestSessionAnswerRepository answerRepository;
    @Mock com.example.sqlix.service.practice.SqlExecutionService sqlExecutionService;
    @InjectMocks MockTestService mockTestService;

    @Test
    void previewRunsAgainstQuestionDatabaseWithoutSavingOrGrading() {
        UUID userId = UUID.randomUUID();
        TestSession session = activeSession();
        var database = new PracticeDatabase();
        database.setInitSqlScript("CREATE TABLE books (book_id int); INSERT INTO books VALUES (1);");
        var question = new MockTestQuestion();
        question.setId(UUID.randomUUID()); question.setTest(session.getTest());
        question.setQuestionType(QuestionType.SQL_QUERY); question.setDatabase(database);
        var result = new com.example.sqlix.dto.response.QueryResultDto(List.of("book_id"), List.of(List.of(1)), null);
        when(sessionRepository.findByIdAndUserId(session.getId(), userId)).thenReturn(Optional.of(session));
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        when(sqlExecutionService.executeSelect(database.getInitSqlScript(), "SELECT book_id FROM books", 10001)).thenReturn(result);
        assertThat(mockTestService.runQuery(userId, session.getId(), question.getId(), "SELECT book_id FROM books")).isEqualTo(result);
        org.mockito.Mockito.verifyNoInteractions(answerRepository);
        org.mockito.Mockito.verify(sqlExecutionService).executeSelect(database.getInitSqlScript(), "SELECT book_id FROM books", 10001);
    }

    @Test
    void previewRejectsQuestionsFromAnotherTestAndMultipleChoice() {
        UUID userId = UUID.randomUUID();
        TestSession session = activeSession();
        var question = new MockTestQuestion(); question.setId(UUID.randomUUID()); question.setTest(mockTest());
        when(sessionRepository.findByIdAndUserId(session.getId(), userId)).thenReturn(Optional.of(session));
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mockTestService.runQuery(userId, session.getId(), question.getId(), "SELECT 1"))
                .isInstanceOf(com.example.sqlix.exception.AppException.class);
        question.setTest(session.getTest()); question.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mockTestService.runQuery(userId, session.getId(), question.getId(), "SELECT 1"))
                .isInstanceOf(com.example.sqlix.exception.AppException.class);
        org.mockito.Mockito.verifyNoInteractions(sqlExecutionService, answerRepository);
    }

    @Test
    void previewRejectsOtherUsersAndExpiredOrFinishedSessions() {
        UUID userId = UUID.randomUUID();
        TestSession session = activeSession(); UUID questionId = UUID.randomUUID();
        when(sessionRepository.findByIdAndUserId(session.getId(), userId)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mockTestService.runQuery(userId, session.getId(), questionId, "SELECT 1"))
                .isInstanceOf(com.example.sqlix.exception.AppException.class);
        when(sessionRepository.findByIdAndUserId(session.getId(), userId)).thenReturn(Optional.of(session));
        session.setStatus(TestSessionStatus.COMPLETED);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mockTestService.runQuery(userId, session.getId(), questionId, "SELECT 1"))
                .isInstanceOf(com.example.sqlix.exception.AppException.class);
        session.setStatus(TestSessionStatus.IN_PROGRESS); session.setStartTime(Instant.now().minusSeconds(3600));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mockTestService.runQuery(userId, session.getId(), questionId, "SELECT 1"))
                .isInstanceOf(com.example.sqlix.exception.AppException.class);
        assertThat(session.getStatus()).isEqualTo(TestSessionStatus.TIMEOUT);
        org.mockito.Mockito.verifyNoInteractions(sqlExecutionService, answerRepository, questionRepository);
    }

    private TestSession activeSession() {
        var session = new TestSession(); session.setId(UUID.randomUUID());
        session.setTest(mockTest()); session.setStartTime(Instant.now());
        return session;
    }

    @Test
    void workspaceMapsSafeQuestionDataWithoutAnswerFields() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        MockTest test = mockTest();
        TestSession session = new TestSession();
        session.setId(sessionId);
        session.setTest(test);
        session.setStartTime(Instant.parse("2026-11-01T08:00:00Z"));

        PracticeDatabase database = new PracticeDatabase();
        database.setId(UUID.randomUUID());
        database.setSchemaData(Map.of("tables", List.of(Map.of("name", "products"))));

        MockTestQuestion sqlQuestion = new MockTestQuestion();
        sqlQuestion.setId(UUID.randomUUID());
        sqlQuestion.setQuestionType(QuestionType.SQL_QUERY);
        sqlQuestion.setContent("Select the product names.");
        sqlQuestion.setPoints(10);
        sqlQuestion.setSortOrder(1);
        sqlQuestion.setDatabase(database);
        sqlQuestion.setRequirements(List.of("Use products"));
        sqlQuestion.setExpectedColumns(List.of("name"));
        sqlQuestion.setSolutionQuery("SECRET SQL ANSWER");

        MockTestQuestion choiceQuestion = new MockTestQuestion();
        choiceQuestion.setId(UUID.randomUUID());
        choiceQuestion.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        choiceQuestion.setContent("Which query reads all rows?");
        choiceQuestion.setPoints(5);
        choiceQuestion.setSortOrder(2);
        choiceQuestion.setMcOptions(List.of(
                Map.of("id", "A", "text", "SELECT * FROM products"),
                Map.of("id", "B", "text", "DROP TABLE products")));
        choiceQuestion.setMcCorrectOption("B");
        choiceQuestion.setExplanation("SECRET EXPLANATION");

        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(questionRepository.findAllByTest_IdOrderBySortOrderAsc(test.getId()))
                .thenReturn(List.of(sqlQuestion, choiceQuestion));

        var workspace = mockTestService.getWorkspace(userId, sessionId);

        assertThat(workspace.questions()).hasSize(2);
        assertThat(workspace.questions().getFirst().schemaData()).isEqualTo(database.getSchemaData());
        assertThat(workspace.questions().getFirst().requirements()).containsExactly("Use products");
        assertThat(workspace.questions().get(1).mcOptions()).hasSize(2);
        Set<String> publicFields = Arrays.stream(WorkspaceQuestionDto.class.getRecordComponents())
                .map(component -> component.getName())
                .collect(Collectors.toSet());
        assertThat(publicFields).doesNotContain("solutionQuery", "mcCorrectOption", "explanation");
        assertThat(workspace.questions()).allSatisfy(question -> {
            assertThat(question.questionType()).isNotNull();
        });
    }

    @Test
    void startSessionCreatesInProgressSessionAndUsesTestDuration() {
        UUID userId = UUID.randomUUID();
        MockTest test = mockTest();
        when(mockTestRepository.findById(test.getId())).thenReturn(Optional.of(test));
        when(userRepository.getReferenceById(userId)).thenReturn(com.example.sqlix.entity.User.builder().id(userId).build());
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> {
            TestSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return session;
        });

        var response = mockTestService.startSession(userId, test.getId());

        assertThat(response.sessionId()).isNotNull();
        assertThat(response.status()).isEqualTo(TestSessionStatus.IN_PROGRESS);
        assertThat(response.durationMinutes()).isEqualTo(45);
        assertThat(response.startTime()).isNotNull();
    }

    private MockTest mockTest() {
        MockTest test = new MockTest();
        test.setId(UUID.randomUUID());
        test.setTitle("Practice Test");
        test.setSlug("practice-test");
        test.setDifficulty(DifficultyLevel.MEDIUM);
        test.setDurationMinutes(45);
        test.setTotalQuestions(2);
        return test;
    }
}
