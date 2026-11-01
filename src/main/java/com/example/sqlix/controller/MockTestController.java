package com.example.sqlix.controller;

import com.example.sqlix.dto.Pagination;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.MockTestCatalogDto;
import com.example.sqlix.dto.response.MockTestDetailDto;
import com.example.sqlix.dto.response.SessionStartResponse;
import com.example.sqlix.dto.response.TestWorkspaceDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.service.mocktest.MockTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MockTestController {
    private final MockTestService mockTestService;

    @GetMapping("/mock-tests")
    public ApiResponse<List<MockTestCatalogDto>> getCatalog(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MockTestCatalogDto> result = mockTestService.getCatalog(search, difficulty, page, size);
        int total = Math.toIntExact(result.getTotalElements());
        int currentPage = result.getNumber() + 1;
        int from = total == 0 ? 0 : result.getNumber() * result.getSize() + 1;
        int to = total == 0 ? 0 : from + result.getNumberOfElements() - 1;
        return ApiResponse.<List<MockTestCatalogDto>>builder()
                .code(200)
                .result(result.getContent())
                .pagination(Pagination.builder()
                        .total(total)
                        .per_page(result.getSize())
                        .current_page(currentPage)
                        .last_page(result.getTotalPages())
                        .from(from)
                        .to(to)
                        .build())
                .build();
    }

    @GetMapping("/mock-tests/{slug}")
    public ApiResponse<MockTestDetailDto> getDetail(@PathVariable String slug) {
        return ApiResponse.<MockTestDetailDto>builder()
                .code(200)
                .result(mockTestService.getDetail(slug))
                .build();
    }

    @PostMapping("/mock-tests/{testId}/start")
    public ResponseEntity<ApiResponse<SessionStartResponse>> startSession(@PathVariable UUID testId) {
        SessionStartResponse result = mockTestService.startSession(currentUserId(), testId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SessionStartResponse>builder().code(201).result(result).build());
    }

    @GetMapping("/test-sessions/{sessionId}/workspace")
    public ApiResponse<TestWorkspaceDto> getWorkspace(@PathVariable UUID sessionId) {
        return ApiResponse.<TestWorkspaceDto>builder()
                .code(200)
                .result(mockTestService.getWorkspace(currentUserId(), sessionId))
                .build();
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((User) authentication.getPrincipal()).getId();
    }
}
