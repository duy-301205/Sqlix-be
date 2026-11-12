package com.example.sqlix.controller;

import com.example.sqlix.dto.request.MockTestQuestionRequest;
import com.example.sqlix.dto.request.MockTestRequest;
import com.example.sqlix.dto.response.AdminMockTestQuestionDto;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.MockTestCatalogDto;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.service.mocktest.AdminMockTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMockTestController {
    private final AdminMockTestService adminMockTestService;

    @GetMapping("/mock-tests")
    public ApiResponse<List<MockTestCatalogDto>> getAllMockTests() {
        return ApiResponse.<List<MockTestCatalogDto>>builder()
                .code(200)
                .result(adminMockTestService.getAllMockTests())
                .build();
    }

    @PostMapping("/mock-tests")
    public ResponseEntity<ApiResponse<UUID>> createMockTest(@Valid @RequestBody MockTestRequest request) {
        MockTest test = adminMockTestService.createMockTest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UUID>builder().code(201).result(test.getId()).build());
    }

    @PutMapping("/mock-tests/{id}")
    public ApiResponse<UUID> updateMockTest(
            @PathVariable UUID id, @Valid @RequestBody MockTestRequest request) {
        return ApiResponse.<UUID>builder().code(200)
                .result(adminMockTestService.updateMockTest(id, request).getId()).build();
    }

    @DeleteMapping("/mock-tests/{id}")
    public ApiResponse<Boolean> deleteMockTest(@PathVariable UUID id) {
        adminMockTestService.deleteMockTest(id);
        return ApiResponse.<Boolean>builder().code(200).result(true).build();
    }

    @GetMapping("/mock-tests/{testId}/questions")
    public ApiResponse<List<AdminMockTestQuestionDto>> getQuestions(@PathVariable UUID testId) {
        return ApiResponse.<List<AdminMockTestQuestionDto>>builder()
                .code(200)
                .result(adminMockTestService.getQuestionsByTestId(testId))
                .build();
    }

    @PostMapping("/mock-tests/{testId}/questions")
    public ResponseEntity<ApiResponse<UUID>> createQuestion(
            @PathVariable UUID testId, @Valid @RequestBody MockTestQuestionRequest request) {
        MockTestQuestion question = adminMockTestService.createQuestion(testId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UUID>builder().code(201).result(question.getId()).build());
    }

    @PutMapping("/mock-test-questions/{questionId}")
    public ApiResponse<UUID> updateQuestion(
            @PathVariable UUID questionId, @Valid @RequestBody MockTestQuestionRequest request) {
        return ApiResponse.<UUID>builder().code(200)
                .result(adminMockTestService.updateQuestion(questionId, request).getId()).build();
    }

    @DeleteMapping("/mock-test-questions/{questionId}")
    public ApiResponse<Boolean> deleteQuestion(@PathVariable UUID questionId) {
        adminMockTestService.deleteQuestion(questionId);
        return ApiResponse.<Boolean>builder().code(200).result(true).build();
    }
}
