package com.example.sqlix.controller;

import com.example.sqlix.dto.request.CourseCategoryRequest;
import com.example.sqlix.dto.request.LessonAdminRequest;
import com.example.sqlix.dto.request.LessonSectionAdminRequest;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.service.learning.AdminLearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/learning")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminLearningController {
    private final AdminLearningService adminLearningService;

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<UUID>> createCategory(@Valid @RequestBody CourseCategoryRequest request) {
        return created(adminLearningService.createCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    public ApiResponse<UUID> updateCategory(
            @PathVariable UUID categoryId, @Valid @RequestBody CourseCategoryRequest request) {
        return ok(adminLearningService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Boolean> deleteCategory(@PathVariable UUID categoryId) {
        adminLearningService.deleteCategory(categoryId);
        return ApiResponse.<Boolean>builder().code(200).result(true).build();
    }

    @PostMapping("/lessons")
    public ResponseEntity<ApiResponse<UUID>> createLesson(@Valid @RequestBody LessonAdminRequest request) {
        return created(adminLearningService.createLesson(request));
    }

    @PutMapping("/lessons/{lessonId}")
    public ApiResponse<UUID> updateLesson(
            @PathVariable UUID lessonId, @Valid @RequestBody LessonAdminRequest request) {
        return ok(adminLearningService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ApiResponse<Boolean> deleteLesson(@PathVariable UUID lessonId) {
        adminLearningService.deleteLesson(lessonId);
        return ApiResponse.<Boolean>builder().code(200).result(true).build();
    }

    @PostMapping("/lessons/{lessonId}/sections")
    public ResponseEntity<ApiResponse<UUID>> createSection(
            @PathVariable UUID lessonId, @Valid @RequestBody LessonSectionAdminRequest request) {
        return created(adminLearningService.createSection(lessonId, request));
    }

    @PutMapping("/sections/{sectionId}")
    public ApiResponse<UUID> updateSection(
            @PathVariable UUID sectionId, @Valid @RequestBody LessonSectionAdminRequest request) {
        return ok(adminLearningService.updateSection(sectionId, request));
    }

    @DeleteMapping("/sections/{sectionId}")
    public ApiResponse<Boolean> deleteSection(@PathVariable UUID sectionId) {
        adminLearningService.deleteSection(sectionId);
        return ApiResponse.<Boolean>builder().code(200).result(true).build();
    }

    private ResponseEntity<ApiResponse<UUID>> created(UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UUID>builder().code(201).result(id).build());
    }

    private ApiResponse<UUID> ok(UUID id) {
        return ApiResponse.<UUID>builder().code(200).result(id).build();
    }
}
