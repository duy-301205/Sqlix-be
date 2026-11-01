package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.AnswerRequestDto;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.entity.TestSessionAnswer;
import com.example.sqlix.enums.QuestionType;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.repository.MockTestQuestionRepository;
import com.example.sqlix.repository.MockTestRepository;
import com.example.sqlix.repository.TestSessionAnswerRepository;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.service.practice.SqlExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockTestSubmissionServiceTest {
    @Mock MockTestRepository mockTestRepository;
    @Mock MockTestQuestionRepository questionRepository;
    @Mock TestSessionRepository sessionRepository;
    @Mock UserRepository userRepository;
    @Mock TestSessionAnswerRepository answerRepository;
    @Mock SqlExecutionService sqlExecutionService;
    @InjectMocks MockTestService service;

    @Test
    void submitTestAddsMultipleChoiceAndSqlScores() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        MockTest test = test();
        TestSession session = session(userId, sessionId, test, Instant.now());
        MockTestQuestion mc = question(test, QuestionType.MULTIPLE_CHOICE, 5);
        mc.setMcCorrectOption("B");
        MockTestQuestion sql = question(test, QuestionType.SQL_QUERY, 20);
        TestSessionAnswer mcAnswer = answer(session, mc, "B", 0);
        TestSessionAnswer sqlAnswer = answer(session, sql, "SELECT 1", 12);
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(questionRepository.findAllByTest_IdOrderBySortOrderAsc(test.getId())).thenReturn(List.of(mc, sql));
        when(answerRepository.findAllBySession_Id(sessionId)).thenReturn(List.of(mcAnswer, sqlAnswer));

        var result = service.submitTest(userId, sessionId);

        assertThat(result.totalScore()).isEqualTo(17);
        assertThat(result.status()).isEqualTo(TestSessionStatus.COMPLETED);
        assertThat(session.getTotalScore()).isEqualTo(17);
        assertThat(mcAnswer.getIsCorrect()).isTrue();
        assertThat(mcAnswer.getScore()).isEqualTo(5);
        verify(sessionRepository).save(session);
    }

    @Test
    void submitAnswerAfterDeadlineClosesSessionAndThrows() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        TestSession session = session(userId, sessionId, test(), Instant.now().minusSeconds(600));
        session.getTest().setDurationMinutes(1);
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.submitAnswer(
                userId, sessionId, UUID.randomUUID(), new AnswerRequestDto("A")))
                .isInstanceOf(AppException.class);

        assertThat(session.getStatus()).isEqualTo(TestSessionStatus.TIMEOUT);
        assertThat(session.getEndTime()).isNotNull();
        verify(sessionRepository).saveAndFlush(session);
    }

    private MockTest test() {
        MockTest test = new MockTest();
        test.setId(UUID.randomUUID());
        test.setDurationMinutes(60);
        return test;
    }

    private TestSession session(UUID userId, UUID sessionId, MockTest test, Instant started) {
        TestSession session = new TestSession();
        session.setId(sessionId);
        session.setTest(test);
        session.setStartTime(started);
        session.setStatus(TestSessionStatus.IN_PROGRESS);
        return session;
    }

    private MockTestQuestion question(MockTest test, QuestionType type, int points) {
        MockTestQuestion question = new MockTestQuestion();
        question.setId(UUID.randomUUID());
        question.setTest(test);
        question.setQuestionType(type);
        question.setPoints(points);
        question.setSortOrder(1);
        return question;
    }

    private TestSessionAnswer answer(TestSession session, MockTestQuestion question, String submitted, int score) {
        TestSessionAnswer answer = new TestSessionAnswer();
        answer.setId(UUID.randomUUID());
        answer.setSession(session);
        answer.setQuestion(question);
        answer.setSubmittedAnswer(submitted);
        answer.setScore(score);
        answer.setIsCorrect(score > 0);
        answer.setRetryCount(1);
        return answer;
    }
}
