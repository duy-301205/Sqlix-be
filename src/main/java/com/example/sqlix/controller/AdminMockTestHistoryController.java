package com.example.sqlix.controller;

import com.example.sqlix.dto.request.AdminHistoryFilterRequest;
import com.example.sqlix.dto.response.AdminHistoryItemDto;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.PagedResponseDto;
import com.example.sqlix.service.mocktest.AdminMockTestHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMockTestHistoryController {
    private final AdminMockTestHistoryService service;

    @GetMapping("/mock-tests/history")
    public ApiResponse<PagedResponseDto<AdminHistoryItemDto>> getAllHistories(@ModelAttribute AdminHistoryFilterRequest request) {
        Page<AdminHistoryItemDto> page = service.getAllHistories(request);
        return ApiResponse.<PagedResponseDto<AdminHistoryItemDto>>builder().code(200).result(PagedResponseDto.from(page)).build();
    }

    @DeleteMapping("/test-sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable UUID sessionId) {
        service.adminDeleteSession(sessionId);
        return ApiResponse.<Void>builder().code(200).build();
    }
}
