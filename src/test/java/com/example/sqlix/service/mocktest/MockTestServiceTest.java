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
    @InjectMocks MockTestService mockTestService;

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
