package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.AdminHistoryFilterRequest;
import com.example.sqlix.entity.TestSession;
import com.example.sqlix.entity.User;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.enums.TestSessionStatus;
import com.example.sqlix.repository.TestSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
        when(repository.findAdminHistory(eq(userId),eq(testId),isNull(),isNull(),eq(50),isNull(),any()))
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
}
