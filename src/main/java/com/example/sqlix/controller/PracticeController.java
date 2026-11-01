package com.example.sqlix.controller;

import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.DatabaseCatalogDto;
import com.example.sqlix.dto.response.DatabaseDetailDto;
import com.example.sqlix.dto.response.WorkspaceDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.practice.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {
    private final PracticeService practiceService;

    @GetMapping("/databases")
    public ApiResponse<List<DatabaseCatalogDto>> getDatabaseCatalog() {
        return ApiResponse.<List<DatabaseCatalogDto>>builder()
                .code(200)
                .result(practiceService.getDatabaseCatalog())
                .build();
    }

    @GetMapping("/databases/{slug}")
    public ApiResponse<DatabaseDetailDto> getDatabaseDetail(@PathVariable String slug) {
        return ApiResponse.<DatabaseDetailDto>builder()
                .code(200)
                .result(practiceService.getDatabaseDetail(currentUserId(), slug))
                .build();
    }

    @GetMapping("/exercises/{exerciseId}")
    public ApiResponse<WorkspaceDto> getWorkspace(@PathVariable UUID exerciseId) {
        return ApiResponse.<WorkspaceDto>builder()
                .code(200)
                .result(practiceService.getWorkspace(currentUserId(), exerciseId))
                .build();
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((User) authentication.getPrincipal()).getId();
    }
}
