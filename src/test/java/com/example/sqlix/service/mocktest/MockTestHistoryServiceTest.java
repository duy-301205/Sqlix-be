package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.HistoryFilterRequest;
import com.example.sqlix.repository.UserMockTestSummaryProjection;
import com.example.sqlix.repository.TestSessionRepository;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.TestSessionAnswer;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.QuestionType;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockTestHistoryServiceTest {
    @Mock TestSessionRepository sessions;
    @InjectMocks MockTestHistoryService service;

    @Test void summaryForUserWithHistory() {
        UUID id=UUID.randomUUID(); UserMockTestSummaryProjection row=mock(UserMockTestSummaryProjection.class);
        when(row.getTotalTestsTaken()).thenReturn(4L); when(row.getAverageScore()).thenReturn(72.5);
        when(row.getHighestScore()).thenReturn(96); when(row.getPassRatePercentage()).thenReturn(75.0);
        when(sessions.findHistorySummaryByUserId(id)).thenReturn(Optional.of(row));
        var result=service.getUserHistorySummary(id);
        assertThat(result.totalTestsTaken()).isEqualTo(4); assertThat(result.averageScore()).isEqualTo(72.5);
        assertThat(result.highestScore()).isEqualTo(96); assertThat(result.passRatePercentage()).isEqualTo(75.0);
    }
    @Test void summaryForNewUserIsAllZeroes() {
        UUID id=UUID.randomUUID(); when(sessions.findHistorySummaryByUserId(id)).thenReturn(Optional.empty());
        var result=service.getUserHistorySummary(id);
        assertThat(result.totalTestsTaken()).isZero(); assertThat(result.averageScore()).isZero();
        assertThat(result.highestScore()).isZero(); assertThat(result.passRatePercentage()).isZero();
    }
    @Test void getsPagedHistoryAndAppliesFilters() {
        UUID id=UUID.randomUUID(); HistoryFilterRequest filter=new HistoryFilterRequest();
        filter.setKeyword("sql"); filter.setStatus("PASSED"); filter.setPage(1); filter.setSize(5);
        when(sessions.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(),PageRequest.of(1,5),0));
        var result=service.getUserHistoryList(id,filter);
        assertThat(result.getNumber()).isEqualTo(1); assertThat(result.getSize()).isEqualTo(5);
        verify(sessions).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class));
    }

    @Test void detailIsScopedToOwnerAndMapsQuestionAnswers() {
        UUID userId=UUID.randomUUID(), sessionId=UUID.randomUUID();
        TestSession session=new TestSession(); session.setId(sessionId); session.setTotalScore(80);
        session.setDurationSeconds(120); session.setIsPassed(true); session.setStatus(TestSessionStatus.COMPLETED);
        MockTest test=new MockTest(); test.setId(UUID.randomUUID()); test.setTitle("SQL basics"); test.setDifficulty(DifficultyLevel.EASY);
        session.setTest(test);
        MockTestQuestion question=new MockTestQuestion(); question.setId(UUID.randomUUID()); question.setSortOrder(1);
        question.setQuestionType(QuestionType.MULTIPLE_CHOICE); question.setContent("Choose"); question.setMcCorrectOption("A");
        question.setExplanation("Because"); question.setPoints(10);
        TestSessionAnswer answer=new TestSessionAnswer(); answer.setQuestion(question); answer.setSubmittedAnswer("A");
        answer.setIsCorrect(true); answer.setScore(10); session.setAnswers(java.util.List.of(answer));
        when(sessions.findByIdAndUserId(sessionId,userId)).thenReturn(Optional.of(session));
        var detail=service.getHistoryDetail(userId,sessionId);
        assertThat(detail.questions()).hasSize(1); assertThat(detail.questions().get(0).correctAnswer()).isEqualTo("A");
        assertThat(detail.questions().get(0).explanation()).isEqualTo("Because");
        verify(sessions).findByIdAndUserId(sessionId,userId);
    }
    @Test void detailDoesNotAllowAnotherUsersSession() {
        UUID userId=UUID.randomUUID(), sessionId=UUID.randomUUID();
        when(sessions.findByIdAndUserId(sessionId,userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getHistoryDetail(userId,sessionId)).isInstanceOf(AppException.class);
    }
    @Test void trendsUseMostRecentWindowAndReturnChronologicalScores() {
        UUID userId=UUID.randomUUID();
        TestSession oldSession=trendSession(20,"First",java.time.Instant.parse("2026-01-01T00:00:00Z"));
        TestSession newSession=trendSession(90,"Second",java.time.Instant.parse("2026-02-01T00:00:00Z"));
        when(sessions.findByUser_IdAndStatusInOrderByCreatedAtDesc(eq(userId),anyList(),any()))
                .thenReturn(java.util.List.of(newSession,oldSession));
        var trends=service.getScoreTrends(userId,2);
        assertThat(trends).extracting("score").containsExactly(20,90);
        assertThat(trends).extracting("testTitle").containsExactly("First","Second");
        verify(sessions).findByUser_IdAndStatusInOrderByCreatedAtDesc(eq(userId),anyList(),any());
    }
    @Test void userCanDeleteOnlyOwnedSession() {
        UUID userId=UUID.randomUUID(), sessionId=UUID.randomUUID(); TestSession session=new TestSession(); session.setId(sessionId);
        when(sessions.findByIdAndUserId(sessionId,userId)).thenReturn(Optional.of(session));
        service.deleteUserSession(userId,sessionId);
        verify(sessions).delete(session);
    }
    private TestSession trendSession(int score,String title,java.time.Instant createdAt) {
        TestSession session=new TestSession(); session.setTotalScore(score); session.setCreatedAt(createdAt);
        session.setEndTime(createdAt); session.setIsPassed(score>=60);
        MockTest test=new MockTest(); test.setTitle(title); session.setTest(test); return session;
    }
}
