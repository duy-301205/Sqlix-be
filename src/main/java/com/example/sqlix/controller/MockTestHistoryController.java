package com.example.sqlix.controller;

import com.example.sqlix.dto.request.HistoryFilterRequest;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.MockTestHistoryItemDto;
import com.example.sqlix.dto.response.MockTestHistorySummaryDto;
import com.example.sqlix.dto.response.PagedResponseDto;
import com.example.sqlix.dto.response.MockTestHistoryDetailDto;
import com.example.sqlix.dto.response.ProgressTrendDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.mocktest.MockTestHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/mock-tests/history")
@RequiredArgsConstructor
public class MockTestHistoryController {
    private final MockTestHistoryService historyService;

    @GetMapping("/summary")
    public ApiResponse<MockTestHistorySummaryDto> getSummary() {
        return ApiResponse.<MockTestHistorySummaryDto>builder().code(200)
                .result(historyService.getUserHistorySummary(currentUserId())).build();
    }

    @GetMapping("/trends")
    public ApiResponse<java.util.List<ProgressTrendDto>> getTrends(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<java.util.List<ProgressTrendDto>>builder().code(200)
                .result(historyService.getScoreTrends(currentUserId(), limit)).build();
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<MockTestHistoryDetailDto> getDetail(@PathVariable UUID sessionId) {
        return ApiResponse.<MockTestHistoryDetailDto>builder().code(200)
                .result(historyService.getHistoryDetail(currentUserId(), sessionId)).build();
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable UUID sessionId) {
        historyService.deleteUserSession(currentUserId(), sessionId);
        return ApiResponse.<Void>builder().code(200).build();
    }

    @GetMapping
    public ApiResponse<PagedResponseDto<MockTestHistoryItemDto>> getHistory(@ModelAttribute HistoryFilterRequest request) {
        Page<MockTestHistoryItemDto> page = historyService.getUserHistoryList(currentUserId(), request);
        return ApiResponse.<PagedResponseDto<MockTestHistoryItemDto>>builder().code(200)
                .result(PagedResponseDto.from(page)).build();
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof User user) return user.getId();
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            String id = jwt.getClaimAsString("userId");
            if (id != null) return UUID.fromString(id);
        }
        throw new org.springframework.security.access.AccessDeniedException("Authenticated user required");
    }
}
