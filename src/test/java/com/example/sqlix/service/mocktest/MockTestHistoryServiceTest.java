package com.example.sqlix.service.mocktest;

import com.example.sqlix.dto.request.HistoryFilterRequest;
import com.example.sqlix.repository.UserMockTestSummaryProjection;
import com.example.sqlix.repository.TestSessionRepository;
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
        when(sessions.findHistory(eq(id),eq("sql"),eq(true),isNull(),any())).thenReturn(new PageImpl<>(java.util.List.of(),PageRequest.of(1,5),0));
        var result=service.getUserHistoryList(id,filter);
        assertThat(result.getNumber()).isEqualTo(1); assertThat(result.getSize()).isEqualTo(5);
        verify(sessions).findHistory(eq(id),eq("sql"),eq(true),isNull(),any());
    }
}
