package com.example.sqlix.controller;

import com.example.sqlix.dto.request.CourseCategoryRequest;
import com.example.sqlix.dto.request.LessonAdminRequest;
import com.example.sqlix.dto.request.LessonSectionAdminRequest;
import com.example.sqlix.service.learning.AdminLearningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminLearningControllerTest {
    @Mock AdminLearningService service;
    @InjectMocks AdminLearningController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createCategoryLessonAndSectionReturnCreated() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        when(service.createCategory(any(CourseCategoryRequest.class))).thenReturn(categoryId);
        when(service.createLesson(any(LessonAdminRequest.class))).thenReturn(lessonId);
        when(service.createSection(eq(lessonId), any(LessonSectionAdminRequest.class))).thenReturn(sectionId);

        mvc.perform(post("/api/admin/learning/categories").contentType("application/json").content(categoryJson()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result").value(categoryId.toString()));
        mvc.perform(post("/api/admin/learning/lessons").contentType("application/json").content(lessonJson(categoryId)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result").value(lessonId.toString()));
        mvc.perform(post("/api/admin/learning/lessons/{lessonId}/sections", lessonId)
                        .contentType("application/json").content(sectionJson()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result").value(sectionId.toString()));
    }

    @Test
    void updateCategoryLessonAndSectionReturnOk() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        when(service.updateCategory(eq(categoryId), any(CourseCategoryRequest.class))).thenReturn(categoryId);
        when(service.updateLesson(eq(lessonId), any(LessonAdminRequest.class))).thenReturn(lessonId);
        when(service.updateSection(eq(sectionId), any(LessonSectionAdminRequest.class))).thenReturn(sectionId);

        mvc.perform(put("/api/admin/learning/categories/{categoryId}", categoryId)
                        .contentType("application/json").content(categoryJson()))
                .andExpect(status().isOk());
        mvc.perform(put("/api/admin/learning/lessons/{lessonId}", lessonId)
                        .contentType("application/json").content(lessonJson(categoryId)))
                .andExpect(status().isOk());
        mvc.perform(put("/api/admin/learning/sections/{sectionId}", sectionId)
                        .contentType("application/json").content(sectionJson()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCategoryLessonAndSectionReturnOk() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();

        mvc.perform(delete("/api/admin/learning/categories/{categoryId}", categoryId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value(true));
        mvc.perform(delete("/api/admin/learning/lessons/{lessonId}", lessonId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value(true));
        mvc.perform(delete("/api/admin/learning/sections/{sectionId}", sectionId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value(true));
    }

    private String categoryJson() {
        return """
                {"name":"SQL Basics","description":"Intro","sortOrder":1}
                """;
    }

    private String lessonJson(UUID categoryId) {
        return """
                {"categoryId":"%s","title":"SELECT","slug":"select-intro",
                 "description":"Intro","objectives":["Read rows"],
                 "practiceTopicCode":"BASIC_SELECT","sortOrder":1}
                """.formatted(categoryId);
    }

    private String sectionJson() {
        return """
                {"title":"SELECT","content":"Lesson text","sampleSql":"SELECT 1",
                 "noteBox":{"type":"tip","text":"Tip"},"sortOrder":1}
                """;
    }
}
