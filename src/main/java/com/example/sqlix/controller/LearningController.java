package com.example.sqlix.controller;

import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.CurriculumCategoryDto;
import com.example.sqlix.dto.response.LessonDetailResponse;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.learning.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {
    private final LearningService learningService;

    @GetMapping("/curriculum")
    public ApiResponse<List<CurriculumCategoryDto>> getCurriculum() {
        return ApiResponse.<List<CurriculumCategoryDto>>builder()
                .code(200)
                .result(learningService.getCurriculumTree(currentUserId()))
                .build();
    }

    @GetMapping("/lessons/{slug}")
    public ApiResponse<LessonDetailResponse> getLessonDetail(@PathVariable String slug) {
        return ApiResponse.<LessonDetailResponse>builder()
                .code(200)
                .result(learningService.getLessonDetail(currentUserId(), slug))
                .build();
    }

    @PostMapping("/lessons/{lessonId}/bookmark")
    public ApiResponse<Boolean> toggleBookmark(@PathVariable UUID lessonId) {
        UUID userId = currentUserId();
        return ApiResponse.<Boolean>builder()
                .code(200)
                .result(learningService.toggleBookmark(userId, lessonId))
                .build();
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ApiResponse<Boolean> markLessonCompleted(@PathVariable UUID lessonId) {
        UUID userId = currentUserId();
        return ApiResponse.<Boolean>builder()
                .code(200)
                .result(learningService.markLessonCompleted(userId, lessonId))
                .build();
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((User) authentication.getPrincipal()).getId();
    }
}
