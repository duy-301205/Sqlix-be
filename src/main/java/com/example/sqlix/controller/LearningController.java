package com.example.sqlix.controller;

import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.CurriculumCategoryDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.learning.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {
    private final LearningService learningService;

    @GetMapping("/curriculum")
    public ApiResponse<List<CurriculumCategoryDto>> getCurriculum() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = ((User) authentication.getPrincipal()).getId();

        return ApiResponse.<List<CurriculumCategoryDto>>builder()
                .code(200)
                .result(learningService.getCurriculumTree(userId))
                .build();
    }
}
