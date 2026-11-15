package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.AdminHistoryFilterRequest;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.entity.User;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.repository.TestSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import org.hibernate.query.criteria.JpaExpression;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMockTestHistoryServiceTest {
    @Mock TestSessionRepository repository;
    @InjectMocks AdminMockTestHistoryService service;

    @Test void filtersAndMapsGlobalHistory() {
        UUID userId=UUID.randomUUID(), testId=UUID.randomUUID(); AdminHistoryFilterRequest filter=new AdminHistoryFilterRequest();
        filter.setUserId(userId); filter.setTestId(testId); filter.setMinScore(50); filter.setPage(0); filter.setSize(10);
        TestSession session=new TestSession(); session.setId(UUID.randomUUID()); session.setTotalScore(88);
        session.setStatus(TestSessionStatus.COMPLETED); session.setDurationSeconds(45); session.setIsPassed(true);
        User user=new User(); user.setId(userId); user.setUsername("learner"); user.setFullName("Learner Name"); session.setUser(user);
        MockTest test=new MockTest(); test.setTitle("Mock SQL"); session.setTest(test);
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(session),PageRequest.of(0,10),1));
        var result=service.getAllHistories(filter);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("learner");
        assertThat(result.getContent().get(0).score()).isEqualTo(88);
    }
    @Test void adminDeleteRemovesSessionAndMissingIdIsNotFound() {
        UUID id=UUID.randomUUID(); TestSession session=new TestSession();
        when(repository.findById(id)).thenReturn(Optional.of(session));
        service.adminDeleteSession(id); verify(repository).delete(session);
    }
    private void applyFilter(AdminHistoryFilterRequest filter, Root<TestSession> root, CriteriaBuilder cb) {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
            Specification<TestSession> spec = invocation.getArgument(0);
            spec.toPredicate(root, mock(CriteriaQuery.class), cb);
            return org.springframework.data.domain.Page.empty();
        });
        service.getAllHistories(filter);
    }

    @ParameterizedTest @EnumSource(TestSessionStatus.class)
    void appliesEverySessionStatusWithoutRestrictingToFinishedSessions(TestSessionStatus status) {
        Root<TestSession> root = mock(Root.class, RETURNS_DEEP_STUBS);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        AdminHistoryFilterRequest filter = new AdminHistoryFilterRequest();
        filter.setStatus(status);
        applyFilter(filter, root, cb);
        verify(cb).equal(root.get("status"), status);
        verify(root.get("status"), never()).in(any(Object[].class));
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void combinesResultWithStatusUserTestDateAndScore(boolean passed) {
        Root<TestSession> root = mock(Root.class, RETURNS_DEEP_STUBS);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        AdminHistoryFilterRequest filter = new AdminHistoryFilterRequest();
        filter.setIsPassed(passed); filter.setStatus(TestSessionStatus.COMPLETED);
        filter.setUserId(UUID.randomUUID()); filter.setTestId(UUID.randomUUID());
        filter.setFromDate(Instant.parse("2026-11-13T17:00:00Z"));
        filter.setToDate(Instant.parse("2026-11-14T16:59:59.999Z"));
        filter.setMinScore(0); filter.setMaxScore(60);
        applyFilter(filter, root, cb);
        verify(cb).equal(root.get("isPassed"), passed);
        verify(cb).equal(root.get("status"), TestSessionStatus.COMPLETED);
        verify(cb).equal(root.get("user").get("id"), filter.getUserId());
        verify(cb).equal(root.get("test").get("id"), filter.getTestId());
        verify(cb).greaterThanOrEqualTo(root.get("endTime"), filter.getFromDate());
        verify(cb).lessThanOrEqualTo(root.get("endTime"), filter.getToDate());
        verify(cb).greaterThanOrEqualTo(root.get("totalScore"), 0);
        verify(cb).lessThanOrEqualTo(root.get("totalScore"), 60);
        var predicates = org.mockito.ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(predicates.capture());
        assertThat(predicates.getValue()).hasSize(8);
    }

    @Test void emptyFilterDoesNotAddHiddenConditions() {
        Root<TestSession> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        applyFilter(new AdminHistoryFilterRequest(), root, cb);
        verifyNoInteractions(root);
        verify(cb).and(new Predicate[0]);
    }

    @Test void keywordSearchesNamesEmailAndPartialSessionIdAsLiteralText() {
        Root<TestSession> root = mock(Root.class, RETURNS_DEEP_STUBS);
        CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
        root.get("user").get("username");
        root.get("user").get("fullName");
        root.get("user").get("email");
        var id = mock(jakarta.persistence.criteria.Path.class, withSettings().extraInterfaces(JpaExpression.class));
        when(root.get("id")).thenReturn(id);
        JpaExpression<String> textId = mock(JpaExpression.class);
        when(((JpaExpression<?>) id).cast(String.class)).thenReturn(textId);
        AdminHistoryFilterRequest filter = new AdminHistoryFilterRequest();
        filter.setKeyword("  Learner_%  ");
        applyFilter(filter, root, cb);
        String pattern = "%learner\\_\\%%";
        verify(cb).like(cb.lower(root.get("user").get("username")), pattern, '\\');
        verify(cb).like(cb.lower(root.get("user").get("fullName")), pattern, '\\');
        verify(cb).like(cb.lower(root.get("user").get("email")), pattern, '\\');
        verify(cb).like(cb.lower(textId), pattern, '\\');
        verify(cb).or(any(Predicate.class), any(Predicate.class), any(Predicate.class), any(Predicate.class));
    }
}
