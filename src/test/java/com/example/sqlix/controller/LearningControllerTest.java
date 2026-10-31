package com.example.sqlix.controller;

import com.example.sqlix.dto.response.CurriculumCategoryDto;
import com.example.sqlix.dto.response.LessonSummaryDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.LessonStatus;
import com.example.sqlix.service.learning.LearningService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LearningControllerTest {
    @Mock LearningService learningService;
    @InjectMocks LearningController learningController;

    private MockMvc mockMvc;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(learningController).build();
        userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("learning-user").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void curriculumReturnsOkWithCurriculumTree() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        when(learningService.getCurriculumTree(userId)).thenReturn(List.of(
                new CurriculumCategoryDto(categoryId, "SQL Basics", "Introduction", 1,
                        List.of(new LessonSummaryDto(lessonId, "SELECT", "select", 1,
                                LessonStatus.NOT_STARTED)))));

        mockMvc.perform(get("/api/learning/curriculum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$.result[0].lessons[0].status").value("NOT_STARTED"));

        verify(learningService).getCurriculumTree(userId);
    }
}
