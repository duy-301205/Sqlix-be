package com.example.sqlix.controller;

import com.example.sqlix.dto.request.HistoryFilterRequest;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.MockTestHistoryItemDto;
import com.example.sqlix.dto.response.MockTestHistorySummaryDto;
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

    @GetMapping
    public ApiResponse<Page<MockTestHistoryItemDto>> getHistory(@ModelAttribute HistoryFilterRequest request) {
        return ApiResponse.<Page<MockTestHistoryItemDto>>builder().code(200)
                .result(historyService.getUserHistoryList(currentUserId(), request)).build();
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
